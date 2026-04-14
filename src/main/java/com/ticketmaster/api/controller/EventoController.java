package com.ticketmaster.api.controller;

import com.ticketmaster.api.exception.ResourceNotFoundException;
import com.ticketmaster.api.model.Evento;
import com.ticketmaster.api.repository.EventoRepository;
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
@RequestMapping("/v1/eventos")
@Tag(name = "Eventos", description = "Gerenciamento de eventos")
public class EventoController {

    @Autowired
    private EventoRepository repository;

    @Operation(summary = "Listar todos os eventos (paginado)")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Evento>>> listar(Pageable pageable) {
        Page<Evento> eventos = repository.findAll(pageable);
        List<EntityModel<Evento>> resources = eventos.stream()
                .map(e -> toModel(e))
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(EventoController.class).listar(pageable)).withSelfRel()));
    }

    @Operation(summary = "Buscar evento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Evento>> buscarPorId(@PathVariable Long id) {
        Evento evento = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));
        return ResponseEntity.ok(toModel(evento));
    }

    @Operation(summary = "Criar novo evento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Evento>> criar(@Valid @RequestBody Evento evento) {
        Evento salvo = repository.save(evento);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(salvo));
    }

    @Operation(summary = "Atualizar evento existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Evento>> atualizar(@PathVariable Long id, @Valid @RequestBody Evento evento) {
        // Garante que o ID existe antes de atualizar
        repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Evento", id));
        evento.setId(id);
        Evento atualizado = repository.save(evento);
        return ResponseEntity.ok(toModel(atualizado));
    }

    @Operation(summary = "Deletar evento")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Evento", id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar eventos por nome (consulta personalizada, paginado)")
    @ApiResponse(responseCode = "200", description = "Resultados encontrados")
    @GetMapping("/busca")
    public ResponseEntity<Page<Evento>> buscarPorNome(@RequestParam String nome, Pageable pageable) {
        return ResponseEntity.ok(repository.findByNomeContainingIgnoreCase(nome, pageable));
    }

    // Monta EntityModel com links HATEOAS: self, update e delete
    private EntityModel<Evento> toModel(Evento evento) {
        return EntityModel.of(evento,
                linkTo(methodOn(EventoController.class).buscarPorId(evento.getId())).withSelfRel(),
                linkTo(methodOn(EventoController.class).atualizar(evento.getId(), evento)).withRel("update"),
                linkTo(methodOn(EventoController.class).deletar(evento.getId())).withRel("delete"));
    }
}