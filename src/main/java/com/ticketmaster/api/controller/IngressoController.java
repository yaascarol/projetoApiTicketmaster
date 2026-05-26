package com.ticketmaster.api.controller;

import com.ticketmaster.api.assemblers.IngressoModelAssembler;
import com.ticketmaster.api.dto.request.IngressoRequest;
import com.ticketmaster.api.dto.response.IngressoResponse;
import com.ticketmaster.api.exception.BusinessException;
import com.ticketmaster.api.exception.ConflictException;
import com.ticketmaster.api.exception.ResourceNotFoundException;
import com.ticketmaster.api.model.Evento;
import com.ticketmaster.api.model.Ingresso;
import com.ticketmaster.api.model.TipoIngresso;
import com.ticketmaster.api.repository.EventoRepository;
import com.ticketmaster.api.repository.IngressoRepository;
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

import java.math.BigDecimal;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/ingressos")
@RequiredArgsConstructor
@Tag(name = "Ingressos", description = "Gerenciamento de ingressos por evento")
public class IngressoController {

    private final IngressoRepository     ingressoRepository;
    private final EventoRepository       eventoRepository;
    private final IngressoModelAssembler assembler;

    private record IngressoFingerprint(Long eventoId, String tipo, String preco) {}
    private record IdempotentIngressoResponse(IngressoFingerprint fingerprint, IngressoResponse body, URI location) {}
    private final ConcurrentHashMap<String, IdempotentIngressoResponse> idempotencyCache = new ConcurrentHashMap<>();
    private final Object idempotencyLock = new Object();

    @Operation(summary = "Listar todos os ingressos")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso.")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<IngressoResponse>>> listar(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<IngressoResponse> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(
                ingressoRepository.findAll(pageable).map(this::toResponse), assembler));
    }

    @Operation(summary = "Buscar ingresso por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingresso encontrado."),
            @ApiResponse(responseCode = "404", description = "Ingresso não encontrado.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<IngressoResponse>> buscarPorId(@PathVariable Long id) {
        Ingresso ingresso = ingressoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingresso", id));
        return ResponseEntity.ok(assembler.toModel(toResponse(ingresso)));
    }

    @Operation(summary = "Buscar ingressos por tipo, preço máximo ou evento")
    @ApiResponse(responseCode = "200", description = "Ingressos encontrados.")
    @GetMapping("/busca")
    public ResponseEntity<PagedModel<EntityModel<IngressoResponse>>> buscar(
            @RequestParam(required = false) TipoIngresso tipo,
            @RequestParam(required = false) BigDecimal precoMax,
            @RequestParam(required = false) Long eventoId,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<IngressoResponse> pagedAssembler) {
        Page<Ingresso> result;
        if (eventoId != null)              result = ingressoRepository.findByEventoId(eventoId, pageable);
        else if (tipo != null && precoMax != null)
            result = ingressoRepository.findByTipoAndPrecoLessThanEqual(tipo, precoMax, pageable);
        else if (tipo != null)             result = ingressoRepository.findByTipo(tipo, pageable);
        else if (precoMax != null)         result = ingressoRepository.findByPrecoLessThanEqual(precoMax, pageable);
        else                               result = ingressoRepository.findAll(pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(result.map(this::toResponse), assembler));
    }

    @Operation(summary = "Criar novo ingresso")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ingresso criado com sucesso.",
                    headers = @Header(name = "Location", description = "URI do recurso criado")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou Idempotency-Key ausente."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "409", description = "Idempotency-Key reutilizada com payload diferente.")
    })
    @PostMapping
    public ResponseEntity<EntityModel<IngressoResponse>> criar(
            @Valid @RequestBody IngressoRequest req) {

        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new BusinessException("O header Idempotency-Key é obrigatório e não pode ser vazio.");

        IngressoFingerprint fingerprint = new IngressoFingerprint(req.getEventoId(),
                req.getTipo() != null ? req.getTipo().name() : null,
                req.getPreco() != null ? req.getPreco().toPlainString() : null);

        synchronized (idempotencyLock) {
            IdempotentIngressoResponse cached = idempotencyCache.get(idempotencyKey);
            if (cached != null) {
                if (!cached.fingerprint().equals(fingerprint))
                    throw new ConflictException("Idempotency-Key já utilizada com um payload diferente.");
                return ResponseEntity.created(cached.location()).body(assembler.toModel(cached.body()));
            }

            Evento evento = eventoRepository.findById(req.getEventoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Evento", req.getEventoId()));

            Ingresso ingresso = new Ingresso();
            ingresso.setEvento(evento); ingresso.setTipo(req.getTipo());
            ingresso.setPreco(req.getPreco()); ingresso.setQuantidadeDisponivel(req.getQuantidadeDisponivel());

            Ingresso salvo = ingressoRepository.save(ingresso);
            IngressoResponse body = toResponse(salvo);
            URI location = URI.create("/ingressos/" + salvo.getId());
            idempotencyCache.put(idempotencyKey, new IdempotentIngressoResponse(fingerprint, body, location));
            return ResponseEntity.created(location).body(assembler.toModel(body));
        }
    }

    @Operation(summary = "Atualizar ingresso existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingresso atualizado."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Ingresso ou evento não encontrado.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<IngressoResponse>> atualizar(
            @PathVariable Long id, @Valid @RequestBody IngressoRequest req) {
        Ingresso existente = ingressoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingresso", id));
        Evento evento = eventoRepository.findById(req.getEventoId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento", req.getEventoId()));
        existente.setEvento(evento); existente.setTipo(req.getTipo());
        existente.setPreco(req.getPreco()); existente.setQuantidadeDisponivel(req.getQuantidadeDisponivel());
        return ResponseEntity.ok(assembler.toModel(toResponse(ingressoRepository.save(existente))));
    }

    @Operation(summary = "Deletar ingresso")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ingresso deletado com sucesso."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Ingresso não encontrado.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!ingressoRepository.existsById(id)) throw new ResourceNotFoundException("Ingresso", id);
        ingressoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private IngressoResponse toResponse(Ingresso i) {
        IngressoResponse r = new IngressoResponse();
        r.setId(i.getId()); r.setTipo(i.getTipo()); r.setPreco(i.getPreco());
        r.setQuantidadeDisponivel(i.getQuantidadeDisponivel());
        if (i.getEvento() != null) { r.setEventoId(i.getEvento().getId()); r.setNomeEvento(i.getEvento().getNome()); }
        return r;
    }
}
