package com.ticketmaster.api.controller;

import com.ticketmaster.api.assemblers.EventoModelAssembler;
import com.ticketmaster.api.dto.request.EventoRequest;
import com.ticketmaster.api.dto.response.EventoResponse;
import com.ticketmaster.api.exception.BusinessException;
import com.ticketmaster.api.exception.ConflictException;
import com.ticketmaster.api.exception.ResourceNotFoundException;
import com.ticketmaster.api.model.Artista;
import com.ticketmaster.api.model.Evento;
import com.ticketmaster.api.model.StatusEvento;
import com.ticketmaster.api.repository.ArtistaRepository;
import com.ticketmaster.api.repository.EventoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos",
     description = "Gerenciamento de eventos. Suporta X-API-Version: v1 (simplificado) e v2 (completo com HATEOAS, padrão).")
public class EventoController {

    private final EventoRepository     eventoRepository;
    private final ArtistaRepository    artistaRepository;
    private final EventoModelAssembler assembler;

    private record EventoFingerprint(String nome, String local, String status) {}
    private record IdempotentEventoResponse(EventoFingerprint fingerprint, EventoResponse body, URI location) {}
    private final ConcurrentHashMap<String, IdempotentEventoResponse> idempotencyCache = new ConcurrentHashMap<>();
    private final Object idempotencyLock = new Object();

    @Operation(summary = "Listar todos os eventos",
               description = "v1 → campos essenciais. v2 → completo com HATEOAS e paginação.",
               parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, example = "v2"))
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso.")
    @GetMapping
    public ResponseEntity<?> listar(@ParameterObject Pageable pageable,
            @RequestHeader(value = "X-API-Version", defaultValue = "v2") String version,
            PagedResourcesAssembler<EventoResponse> pagedAssembler) {
        Page<Evento> page = eventoRepository.findAll(pageable);
        if ("v1".equalsIgnoreCase(version))
            return ResponseEntity.ok(page.stream().map(this::toV1).toList());
        return ResponseEntity.ok(pagedAssembler.toModel(page.map(this::toResponse), assembler));
    }

    @Operation(summary = "Buscar evento por ID",
               parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER, example = "v2"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado."),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id,
            @RequestHeader(value = "X-API-Version", defaultValue = "v2") String version) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));
        if ("v1".equalsIgnoreCase(version)) return ResponseEntity.ok(toV1(evento));
        return ResponseEntity.ok(assembler.toModel(toResponse(evento)));
    }

    @Operation(summary = "Buscar eventos por nome e/ou status")
    @ApiResponse(responseCode = "200", description = "Resultados encontrados.")
    @GetMapping("/busca")
    public ResponseEntity<PagedModel<EntityModel<EventoResponse>>> buscar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) StatusEvento status,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<EventoResponse> pagedAssembler) {
        Page<Evento> result;
        if (nome != null && status != null)
            result = eventoRepository.findByNomeContainingIgnoreCaseAndStatus(nome, status, pageable);
        else if (nome != null) result = eventoRepository.findByNomeContainingIgnoreCase(nome, pageable);
        else if (status != null) result = eventoRepository.findByStatus(status, pageable);
        else result = eventoRepository.findAll(pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(result.map(this::toResponse), assembler));
    }

    @Operation(summary = "Criar novo evento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento criado com sucesso.",
                    headers = @Header(name = "Location", description = "URI do recurso criado")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou Idempotency-Key ausente."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "409", description = "Evento já existe ou Idempotency-Key reutilizada com payload diferente.")
    })
    @PostMapping
    public ResponseEntity<EntityModel<EventoResponse>> criar(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody EventoRequest req) {

        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new BusinessException("O header Idempotency-Key é obrigatório e não pode ser vazio.");

        EventoFingerprint fingerprint = new EventoFingerprint(req.getNome(), req.getLocal(),
                req.getStatus() != null ? req.getStatus().name() : null);

        synchronized (idempotencyLock) {
            IdempotentEventoResponse cached = idempotencyCache.get(idempotencyKey);
            if (cached != null) {
                if (!cached.fingerprint().equals(fingerprint))
                    throw new ConflictException("Idempotency-Key já utilizada com um payload diferente.");
                return ResponseEntity.created(cached.location()).body(assembler.toModel(cached.body()));
            }

            if (eventoRepository.findByNomeContainingIgnoreCase(req.getNome(), Pageable.unpaged())
                    .stream().anyMatch(e -> e.getNome().equalsIgnoreCase(req.getNome())))
                throw new ConflictException("Já existe um evento com o nome: " + req.getNome());

            Evento salvo = eventoRepository.save(toEntity(req));
            EventoResponse body = toResponse(salvo);
            URI location = URI.create("/eventos/" + salvo.getId());
            idempotencyCache.put(idempotencyKey, new IdempotentEventoResponse(fingerprint, body, location));
            return ResponseEntity.created(location).body(assembler.toModel(body));
        }
    }

    @Operation(summary = "Atualizar evento existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento atualizado."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<EventoResponse>> atualizar(
            @PathVariable Long id, @Valid @RequestBody EventoRequest req) {
        Evento existente = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));
        existente.setNome(req.getNome()); existente.setDescricao(req.getDescricao());
        existente.setDataEvento(req.getDataEvento()); existente.setLocal(req.getLocal());
        existente.setStatus(req.getStatus());
        if (req.getArtistaIds() != null)
            existente.setArtistas(artistaRepository.findAllById(req.getArtistaIds()));
        return ResponseEntity.ok(assembler.toModel(toResponse(eventoRepository.save(existente))));
    }

    @Operation(summary = "Deletar evento")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento deletado com sucesso."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!eventoRepository.existsById(id)) throw new ResourceNotFoundException("Evento", id);
        eventoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Evento toEntity(EventoRequest req) {
        Evento e = new Evento();
        e.setNome(req.getNome()); e.setDescricao(req.getDescricao());
        e.setDataEvento(req.getDataEvento()); e.setLocal(req.getLocal()); e.setStatus(req.getStatus());
        if (req.getArtistaIds() != null && !req.getArtistaIds().isEmpty())
            e.setArtistas(artistaRepository.findAllById(req.getArtistaIds()));
        return e;
    }

    private EventoResponse toResponse(Evento e) {
        EventoResponse r = new EventoResponse();
        r.setId(e.getId()); r.setNome(e.getNome()); r.setDescricao(e.getDescricao());
        r.setDataEvento(e.getDataEvento()); r.setLocal(e.getLocal()); r.setStatus(e.getStatus());
        r.setArtistas(e.getArtistas() != null ? e.getArtistas().stream().map(Artista::getNome).toList() : List.of());
        r.setTotalIngressos(e.getIngressos() != null ? e.getIngressos().size() : 0);
        return r;
    }

    private Map<String, Object> toV1(Evento e) {
        return Map.of("id", e.getId(), "nome", e.getNome(), "local", e.getLocal(),
                "dataEvento", e.getDataEvento() != null ? e.getDataEvento().toString() : "",
                "status", e.getStatus() != null ? e.getStatus().name() : "", "version", "v1");
    }
}
