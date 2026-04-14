package com.ticketmaster.api.controller;

import com.ticketmaster.api.exception.ResourceNotFoundException;
import com.ticketmaster.api.model.Ingresso;
import com.ticketmaster.api.model.TipoIngresso;
import com.ticketmaster.api.repository.IngressoRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/ingressos")
@Tag(name = "Ingressos", description = "Gerenciamento de ingressos")
public class IngressoController {

    @Autowired
    private IngressoRepository repository;

    // GET 1 — lista todos
    @Operation(summary = "Listar todos os ingressos (paginado)")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Ingresso>>> listar(Pageable pageable) {
        Page<Ingresso> page = repository.findAll(pageable);
        List<EntityModel<Ingresso>> resources = page.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(IngressoController.class).listar(pageable)).withSelfRel()));
    }

    // GET 2 — por ID
    @Operation(summary = "Buscar ingresso por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingresso encontrado"),
            @ApiResponse(responseCode = "404", description = "Ingresso não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Ingresso>> buscarPorId(@PathVariable Long id) {
        Ingresso obj = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingresso", id));
        return ResponseEntity.ok(toModel(obj));
    }

    // GET 3 — consulta personalizada: filtra por tipo e/ou preço máximo
    @Operation(summary = "Buscar ingressos por tipo e/ou preço máximo (consulta personalizada, paginado)",
            description = "Informe o tipo (ex: VIP, PISTA), o preço máximo, ou ambos. Todos os parâmetros são opcionais.")
    @ApiResponse(responseCode = "200", description = "Ingressos encontrados")
    @GetMapping("/busca")
    public ResponseEntity<Page<Ingresso>> buscar(
            @RequestParam(required = false) TipoIngresso tipo,
            @RequestParam(required = false) BigDecimal precoMax,
            Pageable pageable) {
        if (tipo != null && precoMax != null) {
            return ResponseEntity.ok(repository.findByTipoAndPrecoLessThanEqual(tipo, precoMax, pageable));
        }
        if (tipo != null) {
            return ResponseEntity.ok(repository.findByTipo(tipo, pageable));
        }
        if (precoMax != null) {
            return ResponseEntity.ok(repository.findByPrecoLessThanEqual(precoMax, pageable));
        }
        return ResponseEntity.ok(repository.findAll(pageable));
    }

    @Operation(summary = "Criar novo ingresso")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ingresso criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Ingresso>> criar(@Valid @RequestBody Ingresso obj) {
        Ingresso salvo = repository.save(obj);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(salvo));
    }

    @Operation(summary = "Atualizar ingresso existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingresso atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Ingresso não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Ingresso>> atualizar(@PathVariable Long id, @Valid @RequestBody Ingresso obj) {
        repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ingresso", id));
        obj.setId(id);
        return ResponseEntity.ok(toModel(repository.save(obj)));
    }

    @Operation(summary = "Deletar ingresso")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ingresso deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ingresso não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Ingresso", id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<Ingresso> toModel(Ingresso ingresso) {
        return EntityModel.of(ingresso,
                linkTo(methodOn(IngressoController.class).buscarPorId(ingresso.getId())).withSelfRel(),
                linkTo(methodOn(IngressoController.class).atualizar(ingresso.getId(), ingresso)).withRel("update"),
                linkTo(methodOn(IngressoController.class).excluir(ingresso.getId())).withRel("delete"));
    }
}