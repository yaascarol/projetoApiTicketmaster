package com.ticketmaster.api.controller;

import com.ticketmaster.api.assemblers.PedidoModelAssembler;
import com.ticketmaster.api.dto.request.PedidoRequest;
import com.ticketmaster.api.dto.response.PedidoResponse;
import com.ticketmaster.api.exception.ResourceNotFoundException;
import com.ticketmaster.api.model.Pedido;
import com.ticketmaster.api.model.StatusPedido;
import com.ticketmaster.api.model.Usuario;
import com.ticketmaster.api.repository.PedidoRepository;
import com.ticketmaster.api.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.time.LocalDateTime;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos de ingressos")
public class PedidoController {

    private final PedidoRepository   pedidoRepository;
    private final UsuarioRepository  usuarioRepository;
    private final PedidoModelAssembler assembler;

    @Operation(summary = "Listar todos os pedidos")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso.")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<PedidoResponse>>> listar(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<PedidoResponse> pagedAssembler) {

        Page<PedidoResponse> page = pedidoRepository.findAll(pageable).map(this::toResponse);
        return ResponseEntity.ok(pagedAssembler.toModel(page, assembler));
    }

    @Operation(summary = "Buscar pedido por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado."),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PedidoResponse>> buscarPorId(@PathVariable Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
        return ResponseEntity.ok(assembler.toModel(toResponse(pedido)));
    }

    @Operation(summary = "Buscar pedidos por usuário e/ou status",
               description = "Consulta personalizada paginada. Todos os parâmetros são opcionais.")
    @ApiResponse(responseCode = "200", description = "Pedidos encontrados.")
    @GetMapping("/busca")
    public ResponseEntity<PagedModel<EntityModel<PedidoResponse>>> buscar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) StatusPedido status,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<PedidoResponse> pagedAssembler) {

        Page<Pedido> result;
        if (usuarioId != null && status != null)
            result = pedidoRepository.findByUsuarioIdAndStatus(usuarioId, status, pageable);
        else if (usuarioId != null) result = pedidoRepository.findByUsuarioId(usuarioId, pageable);
        else if (status != null)    result = pedidoRepository.findByStatus(status, pageable);
        else                        result = pedidoRepository.findAll(pageable);

        return ResponseEntity.ok(pagedAssembler.toModel(result.map(this::toResponse), assembler));
    }

    @Operation(summary = "Criar novo pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado."),
            @ApiResponse(responseCode = "409", description = "Requisição duplicada (X-Idempotency-Key já usada).")
    })
    @PostMapping
    public ResponseEntity<EntityModel<PedidoResponse>> criar(
            @Parameter(hidden = true) @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PedidoRequest req) {

        Usuario usuario = usuarioRepository.findById(req.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", req.getUsuarioId()));

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setDataPedido(LocalDateTime.now());

        Pedido salvo = pedidoRepository.save(pedido);
        URI location = URI.create("/pedidos/" + salvo.getId());
        return ResponseEntity.created(location).body(assembler.toModel(toResponse(salvo)));
    }

    @Operation(summary = "Atualizar status do pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido atualizado."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<PedidoResponse>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PedidoRequest req) {

        Pedido existente = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));

        if (req.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(req.getUsuarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", req.getUsuarioId()));
            existente.setUsuario(usuario);
        }

        return ResponseEntity.ok(assembler.toModel(toResponse(pedidoRepository.save(existente))));
    }

    @Operation(summary = "Deletar pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido deletado com sucesso."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!pedidoRepository.existsById(id))
            throw new ResourceNotFoundException("Pedido", id);
        pedidoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private PedidoResponse toResponse(Pedido p) {
        PedidoResponse r = new PedidoResponse();
        r.setId(p.getId());
        r.setStatus(p.getStatus());
        r.setDataPedido(p.getDataPedido());
        r.setTotalItens(p.getItens() != null ? p.getItens().size() : 0);
        if (p.getUsuario() != null) {
            r.setUsuarioId(p.getUsuario().getId());
            r.setNomeUsuario(p.getUsuario().getNome());
        }
        return r;
    }
}
