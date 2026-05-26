package com.ticketmaster.api.controller;

import com.ticketmaster.api.assemblers.ArtistaModelAssembler;
import com.ticketmaster.api.dto.request.ArtistaRequest;
import com.ticketmaster.api.dto.response.ArtistaResponse;
import com.ticketmaster.api.exception.BusinessException;
import com.ticketmaster.api.exception.ConflictException;
import com.ticketmaster.api.exception.ResourceNotFoundException;
import com.ticketmaster.api.model.Artista;
import com.ticketmaster.api.model.GeneroMusical;
import com.ticketmaster.api.repository.ArtistaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/artistas")
@RequiredArgsConstructor
@Tag(name = "Artistas", description = "Gerenciamento de artistas da plataforma")
public class ArtistaController {

    private final ArtistaRepository     artistaRepository;
    private final ArtistaModelAssembler assembler;

    private record ArtistaFingerprint(String nome, String genero) {}

    // cache guarda ArtistaResponse (sem EntityModel) + location
    private record IdempotentArtistaResponse(ArtistaFingerprint fingerprint,
                                             ArtistaResponse body,
                                             URI location) {}

    private final ConcurrentHashMap<String, IdempotentArtistaResponse> idempotencyCache = new ConcurrentHashMap<>();
    private final Object idempotencyLock = new Object();

    @Operation(summary = "Listar todos os artistas", description = "Paginação e ordenação suportadas.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso.")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ArtistaResponse>>> listar(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<ArtistaResponse> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(
                artistaRepository.findAll(pageable).map(this::toResponse), assembler));
    }

    @Operation(summary = "Buscar artista por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artista encontrado."),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ArtistaResponse>> buscarPorId(@PathVariable Long id) {
        Artista artista = artistaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista", id));
        return ResponseEntity.ok(assembler.toModel(toResponse(artista)));
    }

    @Operation(summary = "Buscar artistas por nome ou gênero",
            description = "Consulta personalizada paginada. Todos os parâmetros são opcionais.")
    @ApiResponse(responseCode = "200", description = "Resultados encontrados.")
    @GetMapping("/busca")
    public ResponseEntity<PagedModel<EntityModel<ArtistaResponse>>> buscar(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) GeneroMusical genero,
            PagedResourcesAssembler<ArtistaResponse> pagedAssembler) {
        Page<Artista> result;
        if (nome != null)        result = artistaRepository.findByNomeContainingIgnoreCase(nome, pageable);
        else if (genero != null) result = artistaRepository.findByGenero(genero, pageable);
        else                     result = artistaRepository.findAll(pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(result.map(this::toResponse), assembler));
    }

    @Operation(summary = "Criar novo artista")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Artista criado com sucesso.",
                    headers = @Header(name = "Location", description = "URI do recurso criado")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou Idempotency-Key ausente."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "409", description = "Artista já existe ou Idempotency-Key reutilizada com payload diferente.")
    })
    @PostMapping
    public ResponseEntity<EntityModel<ArtistaResponse>> criar(
            @Valid @RequestBody ArtistaRequest req) {

        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new BusinessException("O header Idempotency-Key é obrigatório e não pode ser vazio.");

        ArtistaFingerprint fingerprint = new ArtistaFingerprint(
                req.getNome(),
                req.getGenero() != null ? req.getGenero().name() : null);

        synchronized (idempotencyLock) {
            IdempotentArtistaResponse cached = idempotencyCache.get(idempotencyKey);
            if (cached != null) {
                if (!cached.fingerprint().equals(fingerprint))
                    throw new ConflictException("Idempotency-Key já utilizada com um payload diferente.");
                // retorna resposta cacheada sem reprocessar
                return ResponseEntity.created(cached.location())
                        .body(assembler.toModel(cached.body()));
            }

            // verifica duplicidade de nome
            boolean nomeExiste = artistaRepository
                    .findByNomeContainingIgnoreCase(req.getNome(), Pageable.unpaged())
                    .stream().anyMatch(a -> a.getNome().equalsIgnoreCase(req.getNome()));
            if (nomeExiste)
                throw new ConflictException("Já existe um artista com o nome: " + req.getNome());

            Artista salvo        = artistaRepository.save(toEntity(req));
            ArtistaResponse body = toResponse(salvo);
            URI location         = URI.create("/artistas/" + salvo.getId());

            idempotencyCache.put(idempotencyKey,
                    new IdempotentArtistaResponse(fingerprint, body, location));

            return ResponseEntity.created(location).body(assembler.toModel(body));
        }
    }

    @Operation(summary = "Atualizar artista existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artista atualizado."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ArtistaResponse>> atualizar(
            @PathVariable Long id, @Valid @RequestBody ArtistaRequest req) {
        Artista existente = artistaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista", id));
        existente.setNome(req.getNome());
        existente.setGenero(req.getGenero());
        existente.setBio(req.getBio());
        existente.setUrlImagem(req.getUrlImagem());
        return ResponseEntity.ok(assembler.toModel(toResponse(artistaRepository.save(existente))));
    }

    @Operation(summary = "Deletar artista")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Artista deletado com sucesso."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!artistaRepository.existsById(id))
            throw new ResourceNotFoundException("Artista", id);
        artistaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Artista toEntity(ArtistaRequest req) {
        Artista a = new Artista();
        a.setNome(req.getNome());
        a.setGenero(req.getGenero());
        a.setBio(req.getBio());
        a.setUrlImagem(req.getUrlImagem());
        return a;
    }

    private ArtistaResponse toResponse(Artista a) {
        ArtistaResponse r = new ArtistaResponse();
        r.setId(a.getId());
        r.setNome(a.getNome());
        r.setGenero(a.getGenero());
        r.setBio(a.getBio());
        r.setUrlImagem(a.getUrlImagem());
        r.setTotalEventos(a.getEventos() != null ? a.getEventos().size() : 0);
        return r;
    }
}
