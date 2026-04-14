package com.ticketmaster.api.controller;

import com.ticketmaster.api.exception.ResourceNotFoundException;
import com.ticketmaster.api.model.Usuario;
import com.ticketmaster.api.repository.UsuarioRepository;
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
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Gerenciamento de usuários")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @Operation(summary = "Listar todos os usuários (paginado)")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Usuario>>> listar(Pageable pageable) {
        Page<Usuario> page = repository.findAll(pageable);
        List<EntityModel<Usuario>> resources = page.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(UsuarioController.class).listar(pageable)).withSelfRel()));
    }

    @Operation(summary = "Buscar usuário por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> buscarPorId(@PathVariable Long id) {
        Usuario obj = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        return ResponseEntity.ok(toModel(obj));
    }

    @Operation(summary = "Criar novo usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Usuario>> criar(@Valid @RequestBody Usuario obj) {
        Usuario salvo = repository.save(obj);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(salvo));
    }

    @Operation(summary = "Atualizar usuário existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> atualizar(@PathVariable Long id, @Valid @RequestBody Usuario obj) {
        repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        obj.setId(id);
        return ResponseEntity.ok(toModel(repository.save(obj)));
    }

    @Operation(summary = "Deletar usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário", id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar usuários por nome (consulta personalizada, paginado)")
    @ApiResponse(responseCode = "200", description = "Resultados encontrados")
    @GetMapping("/busca")
    public ResponseEntity<Page<Usuario>> buscarPorNome(@RequestParam String nome, Pageable pageable) {
        return ResponseEntity.ok(repository.findByNomeContainingIgnoreCase(nome, pageable));
    }

    private EntityModel<Usuario> toModel(Usuario usuario) {
        return EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).buscarPorId(usuario.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).atualizar(usuario.getId(), usuario)).withRel("update"),
                linkTo(methodOn(UsuarioController.class).excluir(usuario.getId())).withRel("delete"));
    }
}