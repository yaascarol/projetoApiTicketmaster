package com.ticketmaster.api.controller;

import com.ticketmaster.api.exception.ResourceNotFoundException;
import com.ticketmaster.api.model.ItemPedido;
import com.ticketmaster.api.repository.ItemPedidoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/itens-pedido")
@Tag(name = "Itens de Pedido", description = "Gerenciamento dos itens de cada pedido")
public class ItemPedidoController {

    @Autowired
    private ItemPedidoRepository repository;

    @Operation(summary = "Listar todos os itens de pedido (paginado)")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ItemPedido>>> listar(Pageable pageable) {
        Page<ItemPedido> page = repository.findAll(pageable);
        List<EntityModel<ItemPedido>> resources = page.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(ItemPedidoController.class).listar(pageable)).withSelfRel()));
    }

    @Operation(summary = "Buscar item de pedido por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ItemPedido>> buscarPorId(@PathVariable Long id) {
        ItemPedido obj = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ItemPedido", id));
        return ResponseEntity.ok(toModel(obj));
    }

    @Operation(summary = "Adicionar item ao pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item adicionado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<EntityModel<ItemPedido>> adicionar(@Valid @RequestBody ItemPedido obj) {
        ItemPedido salvo = repository.save(obj);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(salvo));
    }

    @Operation(summary = "Atualizar item de pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ItemPedido>> atualizar(@PathVariable Long id, @Valid @RequestBody ItemPedido obj) {
        repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ItemPedido", id));
        obj.setId(id);
        return ResponseEntity.ok(toModel(repository.save(obj)));
    }

    @Operation(summary = "Remover item de pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("ItemPedido", id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar itens de um pedido específico (consulta personalizada, paginado)")
    @ApiResponse(responseCode = "200", description = "Itens do pedido retornados")
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Page<ItemPedido>> buscarPorPedido(@PathVariable Long pedidoId, Pageable pageable) {
        return ResponseEntity.ok(repository.findByPedidoId(pedidoId, pageable));
    }

    private EntityModel<ItemPedido> toModel(ItemPedido item) {
        return EntityModel.of(item,
                linkTo(methodOn(ItemPedidoController.class).buscarPorId(item.getId())).withSelfRel(),
                linkTo(methodOn(ItemPedidoController.class).atualizar(item.getId(), item)).withRel("update"),
                linkTo(methodOn(ItemPedidoController.class).excluir(item.getId())).withRel("delete"));
    }
}