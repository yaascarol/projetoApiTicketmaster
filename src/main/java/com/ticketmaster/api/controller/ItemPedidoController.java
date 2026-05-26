package com.ticketmaster.api.controller;

import com.ticketmaster.api.assemblers.ItemPedidoModelAssembler;
import com.ticketmaster.api.dto.request.ItemPedidoRequest;
import com.ticketmaster.api.dto.response.ItemPedidoResponse;
import com.ticketmaster.api.exception.BusinessException;
import com.ticketmaster.api.exception.ConflictException;
import com.ticketmaster.api.exception.ResourceNotFoundException;
import com.ticketmaster.api.model.Ingresso;
import com.ticketmaster.api.model.ItemPedido;
import com.ticketmaster.api.model.Pedido;
import com.ticketmaster.api.repository.IngressoRepository;
import com.ticketmaster.api.repository.ItemPedidoRepository;
import com.ticketmaster.api.repository.PedidoRepository;
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
@RequestMapping("/itens-pedido")
@RequiredArgsConstructor
@Tag(name = "Itens de Pedido", description = "Gerenciamento dos itens de cada pedido")
public class ItemPedidoController {

    private final ItemPedidoRepository     itemPedidoRepository;
    private final PedidoRepository         pedidoRepository;
    private final IngressoRepository       ingressoRepository;
    private final ItemPedidoModelAssembler assembler;

    private record ItemFingerprint(Long pedidoId, Long ingressoId, Integer quantidade) {}
    private record IdempotentItemResponse(ItemFingerprint fingerprint, ItemPedidoResponse body, URI location) {}
    private final ConcurrentHashMap<String, IdempotentItemResponse> idempotencyCache = new ConcurrentHashMap<>();
    private final Object idempotencyLock = new Object();

    @Operation(summary = "Listar todos os itens de pedido")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso.")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<ItemPedidoResponse>>> listar(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<ItemPedidoResponse> pagedAssembler) {
        return ResponseEntity.ok(pagedAssembler.toModel(
                itemPedidoRepository.findAll(pageable).map(this::toResponse), assembler));
    }

    @Operation(summary = "Buscar item de pedido por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item encontrado."),
            @ApiResponse(responseCode = "404", description = "Item não encontrado.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ItemPedidoResponse>> buscarPorId(@PathVariable Long id) {
        ItemPedido item = itemPedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ItemPedido", id));
        return ResponseEntity.ok(assembler.toModel(toResponse(item)));
    }

    @Operation(summary = "Buscar itens por pedido ou ingresso")
    @ApiResponse(responseCode = "200", description = "Itens encontrados.")
    @GetMapping("/busca")
    public ResponseEntity<PagedModel<EntityModel<ItemPedidoResponse>>> buscar(
            @RequestParam(required = false) Long pedidoId,
            @RequestParam(required = false) Long ingressoId,
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<ItemPedidoResponse> pagedAssembler) {
        Page<ItemPedido> result;
        if (pedidoId != null)        result = itemPedidoRepository.findByPedidoId(pedidoId, pageable);
        else if (ingressoId != null) result = itemPedidoRepository.findByIngressoId(ingressoId, pageable);
        else                         result = itemPedidoRepository.findAll(pageable);
        return ResponseEntity.ok(pagedAssembler.toModel(result.map(this::toResponse), assembler));
    }

    @Operation(summary = "Adicionar item ao pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item adicionado com sucesso.",
                    headers = @Header(name = "Location", description = "URI do recurso criado")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou Idempotency-Key ausente."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Pedido ou ingresso não encontrado."),
            @ApiResponse(responseCode = "409", description = "Idempotency-Key reutilizada com payload diferente.")
    })
    @PostMapping
    public ResponseEntity<EntityModel<ItemPedidoResponse>> criar(
            @Valid @RequestBody ItemPedidoRequest req) {

        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new BusinessException("O header Idempotency-Key é obrigatório e não pode ser vazio.");

        ItemFingerprint fingerprint = new ItemFingerprint(req.getPedidoId(), req.getIngressoId(), req.getQuantidade());

        synchronized (idempotencyLock) {
            IdempotentItemResponse cached = idempotencyCache.get(idempotencyKey);
            if (cached != null) {
                if (!cached.fingerprint().equals(fingerprint))
                    throw new ConflictException("Idempotency-Key já utilizada com um payload diferente.");
                return ResponseEntity.created(cached.location()).body(assembler.toModel(cached.body()));
            }

            Pedido  pedido   = pedidoRepository.findById(req.getPedidoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pedido", req.getPedidoId()));
            Ingresso ingresso = ingressoRepository.findById(req.getIngressoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingresso", req.getIngressoId()));

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido); item.setIngresso(ingresso); item.setQuantidade(req.getQuantidade());

            ItemPedido salvo = itemPedidoRepository.save(item);
            ItemPedidoResponse body = toResponse(salvo);
            URI location = URI.create("/itens-pedido/" + salvo.getId());
            idempotencyCache.put(idempotencyKey, new IdempotentItemResponse(fingerprint, body, location));
            return ResponseEntity.created(location).body(assembler.toModel(body));
        }
    }

    @Operation(summary = "Atualizar item de pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item atualizado."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Item não encontrado.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ItemPedidoResponse>> atualizar(
            @PathVariable Long id, @Valid @RequestBody ItemPedidoRequest req) {
        ItemPedido existente = itemPedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ItemPedido", id));
        existente.setQuantidade(req.getQuantidade());
        return ResponseEntity.ok(assembler.toModel(toResponse(itemPedidoRepository.save(existente))));
    }

    @Operation(summary = "Remover item de pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removido com sucesso."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Item não encontrado.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!itemPedidoRepository.existsById(id)) throw new ResourceNotFoundException("ItemPedido", id);
        itemPedidoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ItemPedidoResponse toResponse(ItemPedido item) {
        ItemPedidoResponse r = new ItemPedidoResponse();
        r.setId(item.getId()); r.setQuantidade(item.getQuantidade());
        if (item.getIngresso() != null) {
            r.setIngressoId(item.getIngresso().getId());
            r.setTipoIngresso(item.getIngresso().getTipo());
            r.setPrecoUnitario(item.getIngresso().getPreco());
        }
        if (item.getPedido() != null) r.setPedidoId(item.getPedido().getId());
        return r;
    }
}
