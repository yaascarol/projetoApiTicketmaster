package com.seu.evento.api.controller;

import com.seu.evento.api.model.Evento;
import com.seu.evento.api.repository.EventoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/eventos")
public class EventoController {

    @Autowired
    private EventoRepository repository;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Evento>>> listar(Pageable pageable) {
        Page<Evento> eventos = repository.findAll(pageable);
        List<EntityModel<Evento>> resources = eventos.stream()
                .map(e -> EntityModel.of(e,
                        linkTo(methodOn(EventoController.class).buscarPorId(e.getId())).withSelfRel()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(EventoController.class).listar(pageable)).withSelfRel()));
    }

    @GetMapping("/{id}")
    public EntityModel<Evento> buscarPorId(@PathVariable Long id) {
        Evento evento = repository.findById(id).orElseThrow(() -> new RuntimeException("Evento não encontrado"));
        return EntityModel.of(evento, linkTo(methodOn(EventoController.class).buscarPorId(id)).withSelfRel());
    }

    @PostMapping
    public ResponseEntity<EntityModel<Evento>> criar(@Valid @RequestBody Evento evento) {
        Evento salvo = repository.save(evento);
        return ResponseEntity.status(HttpStatus.CREATED).body(buscarPorId(salvo.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Evento> atualizar(@PathVariable Long id, @Valid @RequestBody Evento evento) {
        evento.setId(id);
        return ResponseEntity.ok(repository.save(evento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/busca")
    public ResponseEntity<Page<Evento>> buscarPorNome(@RequestParam String nome, Pageable pageable) {
        return ResponseEntity.ok(repository.findByNomeContainingIgnoreCase(nome, pageable));
    }
}
