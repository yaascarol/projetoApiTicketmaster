package com.ticketmaster.api.controller;

import com.seu.evento.api.model.ItemPedido;
import com.seu.evento.api.repository.ItemPedidoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/itens-pedido")
public class ItemPedidoController {

    @Autowired
    private ItemPedidoRepository repository;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ItemPedido>>> listar(Pageable pageable) {
        Page<ItemPedido> page = repository.findAll(pageable);
        List<EntityModel<ItemPedido>> resources = page.stream()
                .map(obj -> EntityModel.of(obj,
                        linkTo(methodOn(ItemPedidoController.class).buscarPorId(obj.getId())).withSelfRel()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(ItemPedidoController.class).listar(pageable)).withSelfRel()));
    }

    @GetMapping("/{id}")
    public EntityModel<ItemPedido> buscarPorId(@PathVariable Long id) {
        ItemPedido obj = repository.findById(id).orElseThrow(() -> new RuntimeException("404"));
        return EntityModel.of(obj, linkTo(methodOn(ItemPedidoController.class).buscarPorId(id)).withSelfRel());
    }

    @PostMapping
    public ResponseEntity<EntityModel<ItemPedido>> adicionar(@Valid @RequestBody ItemPedido obj) {
        ItemPedido salvo = repository.save(obj);
        return ResponseEntity.status(201).body(buscarPorId(salvo.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemPedido> atualizar(@PathVariable Long id, @Valid @RequestBody ItemPedido obj) {
        obj.setId(id);
        return ResponseEntity.ok(repository.save(obj));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Page<ItemPedido>> buscarPorPedido(@PathVariable Long pedidoId, Pageable pageable) {
        return ResponseEntity.ok(repository.findByPedidoId(pedidoId, pageable));
    }
}
