package com.ticketmaster.api.controller;

import com.seu.evento.api.model.Pedido;
import com.seu.evento.api.repository.PedidoRepository;
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
@RequestMapping("/v1/pedidos")
public class PedidoController {

    @Autowired
    private PedidoRepository repository;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Pedido>>> listar(Pageable pageable) {
        Page<Pedido> page = repository.findAll(pageable);
        List<EntityModel<Pedido>> resources = page.stream()
                .map(obj -> EntityModel.of(obj,
                        linkTo(methodOn(PedidoController.class).buscarPorId(obj.getId())).withSelfRel()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(PedidoController.class).listar(pageable)).withSelfRel()));
    }

    @GetMapping("/{id}")
    public EntityModel<Pedido> buscarPorId(@PathVariable Long id) {
        Pedido obj = repository.findById(id).orElseThrow(() -> new RuntimeException("404"));
        return EntityModel.of(obj, linkTo(methodOn(PedidoController.class).buscarPorId(id)).withSelfRel());
    }

    @PostMapping
    public ResponseEntity<EntityModel<Pedido>> criar(@Valid @RequestBody Pedido obj) {
        if (obj.getDataPedido() == null) {
            obj.setDataPedido(java.time.LocalDateTime.now());
        }
        Pedido salvo = repository.save(obj);
        return ResponseEntity.status(201).body(buscarPorId(salvo.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pedido> atualizarStatus(@PathVariable Long id, @Valid @RequestBody Pedido obj) {
        obj.setId(id);
        return ResponseEntity.ok(repository.save(obj));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Page<Pedido>> buscarPorUsuario(@PathVariable Long usuarioId, Pageable pageable) {
        return ResponseEntity.ok(repository.findByUsuarioId(usuarioId, pageable));
    }
}
