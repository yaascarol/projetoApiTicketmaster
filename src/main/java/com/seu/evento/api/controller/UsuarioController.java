package com.seu.evento.api.controller;

import com.seu.evento.api.model.Usuario;
import com.seu.evento.api.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Usuario>>> listar(Pageable pageable) {
        Page<Usuario> page = repository.findAll(pageable);
        List<EntityModel<Usuario>> resources = page.stream()
                .map(obj -> EntityModel.of(obj,
                        linkTo(methodOn(UsuarioController.class).buscarPorId(obj.getId())).withSelfRel()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(UsuarioController.class).listar(pageable)).withSelfRel()));
    }

    @GetMapping("/{id}")
    public EntityModel<Usuario> buscarPorId(@PathVariable Long id) {
        Usuario obj = repository.findById(id).orElseThrow(() -> new RuntimeException("404"));
        return EntityModel.of(obj, linkTo(methodOn(UsuarioController.class).buscarPorId(id)).withSelfRel());
    }

@PostMapping
public ResponseEntity<EntityModel<Usuario>> criar(@Valid @RequestBody Usuario obj) {
    Usuario salvo = repository.save(obj);
    return ResponseEntity.status(201).body(buscarPorId(salvo.getId()));
}

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(@PathVariable Long id, @Valid @RequestBody Usuario obj) {
        obj.setId(id);
        return ResponseEntity.ok(repository.save(obj));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/busca")
    public ResponseEntity<Page<Usuario>> buscarPorNome(@RequestParam String nome, Pageable pageable) {
        return ResponseEntity.ok(repository.findByNomeContainingIgnoreCase(nome, pageable));
    }
}
