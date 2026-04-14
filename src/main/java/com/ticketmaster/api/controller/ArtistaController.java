package com.ticketmaster.api.controller;

import com.ticketmaster.api.exception.ResourceNotFoundException;
import com.ticketmaster.api.model.Artista;
import com.ticketmaster.api.model.GeneroMusical;
import com.ticketmaster.api.repository.ArtistaRepository;
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
@RequestMapping("/artistas")
@Tag(name = "Artistas", description = "Gerenciamento de artistas")
public class ArtistaController {

    @Autowired
    private ArtistaRepository repository;

    @Operation(summary = "Listar todos os artistas (paginado)")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Artista>>> listar(Pageable pageable) {
        Page<Artista> page = repository.findAll(pageable);
        List<EntityModel<Artista>> resources = page.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(ArtistaController.class).listar(pageable)).withSelfRel()));
    }

    @Operation(summary = "Buscar artista por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artista encontrado"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Artista>> buscarPorId(@PathVariable Long id) {
        Artista obj = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artista", id));
        return ResponseEntity.ok(toModel(obj));
    }

    @Operation(summary = "Criar novo artista")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Artista criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Artista>> criar(@Valid @RequestBody Artista obj) {
        Artista salvo = repository.save(obj);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(salvo));
    }

    @Operation(summary = "Atualizar artista existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artista atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Artista>> atualizar(@PathVariable Long id, @Valid @RequestBody Artista obj) {
        repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Artista", id));
        obj.setId(id);
        return ResponseEntity.ok(toModel(repository.save(obj)));
    }

    @Operation(summary = "Deletar artista")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Artista deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Artista não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Artista", id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar artistas por nome (consulta personalizada, paginado)")
    @ApiResponse(responseCode = "200", description = "Resultados encontrados")
    @GetMapping("/busca")
    public ResponseEntity<Page<Artista>> buscarPorNome(@RequestParam String nome, Pageable pageable) {
        return ResponseEntity.ok(repository.findByNomeContainingIgnoreCase(nome, pageable));
    }

    private EntityModel<Artista> toModel(Artista artista) {
        return EntityModel.of(artista,
                linkTo(methodOn(ArtistaController.class).buscarPorId(artista.getId())).withSelfRel(),
                linkTo(methodOn(ArtistaController.class).atualizar(artista.getId(), artista)).withRel("update"),
                linkTo(methodOn(ArtistaController.class).excluir(artista.getId())).withRel("delete"));
    }
}