package com.ticketmaster.api.controller;

import com.ticketmaster.api.assemblers.EventoModelAssembler;
import com.ticketmaster.api.dto.request.EventoRequest;
import com.ticketmaster.api.dto.response.EventoResponse;
import com.ticketmaster.api.exception.ResourceNotFoundException;
import com.ticketmaster.api.model.Artista;
import com.ticketmaster.api.model.Evento;
import com.ticketmaster.api.model.StatusEvento;
import com.ticketmaster.api.repository.ArtistaRepository;
import com.ticketmaster.api.repository.EventoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos",
     description = "Gerenciamento de eventos. Suporta X-API-Version: v1 (simplificado) e v2 (completo com HATEOAS, padrão).")
public class EventoController {

    private final EventoRepository    eventoRepository;
    private final ArtistaRepository   artistaRepository;
    private final EventoModelAssembler assembler;

    @Operation(
            summary = "Listar todos os eventos",
            description = "v1 → retorna apenas campos essenciais. v2 → resposta completa com HATEOAS e metadados de paginação.",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER,
                    description = "Versão da resposta (v1 ou v2)", example = "v2")
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso.")
    @ApiResponse(responseCode = "400", description = "Versão inválida no header X-API-Version.")
    @GetMapping
    public ResponseEntity<?> listar(
            @ParameterObject Pageable pageable,
            @RequestHeader(value = "X-API-Version", defaultValue = "v2") String version,
            PagedResourcesAssembler<EventoResponse> pagedAssembler) {

        Page<Evento> page = eventoRepository.findAll(pageable);

        if ("v1".equalsIgnoreCase(version)) {
            List<Map<String, Object>> resumo = page.stream().map(this::toV1).toList();
            return ResponseEntity.ok(resumo);
        }

        return ResponseEntity.ok(pagedAssembler.toModel(page.map(this::toResponse), assembler));
    }

    @Operation(
            summary = "Buscar evento por ID",
            parameters = @Parameter(name = "X-API-Version", in = ParameterIn.HEADER,
                    description = "v1 = simplificado · v2 = completo com HATEOAS (padrão)", example = "v2")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado."),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(
            @PathVariable Long id,
            @RequestHeader(value = "X-API-Version", defaultValue = "v2") String version) {

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));

        if ("v1".equalsIgnoreCase(version)) return ResponseEntity.ok(toV1(evento));
        return ResponseEntity.ok(assembler.toModel(toResponse(evento)));
    }

    @Operation(summary = "Buscar eventos por nome e/ou status",
               description = "Consulta personalizada paginada. Todos os parâmetros são opcionais.")
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
        else if (nome != null)
            result = eventoRepository.findByNomeContainingIgnoreCase(nome, pageable);
        else if (status != null)
            result = eventoRepository.findByStatus(status, pageable);
        else
            result = eventoRepository.findAll(pageable);

        return ResponseEntity.ok(pagedAssembler.toModel(result.map(this::toResponse), assembler));
    }

    @Operation(summary = "Criar novo evento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "409", description = "Requisição duplicada (X-Idempotency-Key já usada).")
    })
    @PostMapping
    public ResponseEntity<EntityModel<EventoResponse>> criar(
            @Parameter(hidden = true) @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody EventoRequest req) {

        Evento evento = toEntity(req);
        Evento salvo  = eventoRepository.save(evento);
        URI location  = URI.create("/eventos/" + salvo.getId());
        return ResponseEntity.created(location).body(assembler.toModel(toResponse(salvo)));
    }

    @Operation(summary = "Atualizar evento existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento atualizado."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<EventoResponse>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EventoRequest req) {

        Evento existente = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));

        existente.setNome(req.getNome());
        existente.setDescricao(req.getDescricao());
        existente.setDataEvento(req.getDataEvento());
        existente.setLocal(req.getLocal());
        existente.setStatus(req.getStatus());

        if (req.getArtistaIds() != null) {
            List<Artista> artistas = artistaRepository.findAllById(req.getArtistaIds());
            existente.setArtistas(artistas);
        }

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
        if (!eventoRepository.existsById(id))
            throw new ResourceNotFoundException("Evento", id);
        eventoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Evento toEntity(EventoRequest req) {
        Evento e = new Evento();
        e.setNome(req.getNome());
        e.setDescricao(req.getDescricao());
        e.setDataEvento(req.getDataEvento());
        e.setLocal(req.getLocal());
        e.setStatus(req.getStatus());

        if (req.getArtistaIds() != null && !req.getArtistaIds().isEmpty()) {
            List<Artista> artistas = artistaRepository.findAllById(req.getArtistaIds());
            e.setArtistas(artistas);
        }
        return e;
    }

    private EventoResponse toResponse(Evento e) {
        EventoResponse r = new EventoResponse();
        r.setId(e.getId());
        r.setNome(e.getNome());
        r.setDescricao(e.getDescricao());
        r.setDataEvento(e.getDataEvento());
        r.setLocal(e.getLocal());
        r.setStatus(e.getStatus());
        r.setArtistas(e.getArtistas() != null
                ? e.getArtistas().stream().map(Artista::getNome).toList()
                : List.of());
        r.setTotalIngressos(e.getIngressos() != null ? e.getIngressos().size() : 0);
        return r;
    }

    /** Versão v1 — resposta simplificada sem HATEOAS. */
    private Map<String, Object> toV1(Evento e) {
        return Map.of(
                "id",         e.getId(),
                "nome",       e.getNome(),
                "local",      e.getLocal(),
                "dataEvento", e.getDataEvento() != null ? e.getDataEvento().toString() : "",
                "status",     e.getStatus() != null ? e.getStatus().name() : "",
                "version",    "v1"
        );
    }
}
