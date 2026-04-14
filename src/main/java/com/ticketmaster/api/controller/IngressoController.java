package com.ticketmaster.api.controller;

import com.seu.evento.api.model.Ingresso;
import com.seu.evento.api.repository.IngressoRepository;
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
@RequestMapping("/v1/ingressos")
public class IngressoController {

    @Autowired
    private IngressoRepository repository;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Ingresso>>> listar(Pageable pageable) {
        Page<Ingresso> page = repository.findAll(pageable);
        List<EntityModel<Ingresso>> resources = page.stream()
                .map(obj -> EntityModel.of(obj,
                        linkTo(methodOn(IngressoController.class).buscarPorId(obj.getId())).withSelfRel()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(IngressoController.class).listar(pageable)).withSelfRel()));
    }

    @GetMapping("/{id}")
    public EntityModel<Ingresso> buscarPorId(@PathVariable Long id) {
        Ingresso obj = repository.findById(id).orElseThrow(() -> new RuntimeException("404"));
        return EntityModel.of(obj, linkTo(methodOn(IngressoController.class).buscarPorId(id)).withSelfRel());
    }

    @PostMapping
    public ResponseEntity<EntityModel<Ingresso>> criar(@Valid @RequestBody Ingresso obj) {
        Ingresso salvo = repository.save(obj);
        return ResponseEntity.status(201).body(buscarPorId(salvo.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ingresso> atualizar(@PathVariable Long id, @Valid @RequestBody Ingresso obj) {
        obj.setId(id);
        return ResponseEntity.ok(repository.save(obj));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/busca")
    public ResponseEntity<Page<Ingresso>> buscarPorPreco(@RequestParam java.math.BigDecimal preco, Pageable pageable) {
        return ResponseEntity.ok(repository.findByPrecoLessThanEqual(preco, pageable));
    }
}
