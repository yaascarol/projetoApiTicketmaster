package com.ticketmaster.api.controller;

import com.ticketmaster.api.exception.ConflictException;
import com.ticketmaster.api.exception.ResourceNotFoundException;
import com.ticketmaster.api.model.Usuario;
import com.ticketmaster.api.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários da plataforma")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    @Operation(summary = "Listar todos os usuários")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso.")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Usuario>>> listar(@ParameterObject Pageable pageable) {
        Page<Usuario> page = usuarioRepository.findAll(pageable);
        List<EntityModel<Usuario>> resources = page.stream().map(this::toModel).toList();
        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(UsuarioController.class).listar(pageable)).withSelfRel()));
    }

    @Operation(summary = "Buscar usuário por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> buscarPorId(@PathVariable Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        return ResponseEntity.ok(toModel(u));
    }

    @Operation(summary = "Buscar usuários por nome")
    @ApiResponse(responseCode = "200", description = "Resultados encontrados.")
    @GetMapping("/busca")
    public ResponseEntity<Page<Usuario>> buscarPorNome(@RequestParam String nome,
                                                        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(usuarioRepository.findByNomeContainingIgnoreCase(nome, pageable));
    }

    @Operation(summary = "Criar novo usuário",
               description = "Cria um novo usuário da plataforma. Para obter uma X-API-Key, use POST /api/auth/api-keys.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos."),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado.")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Usuario>> criar(@Valid @RequestBody Usuario obj) {
        if (usuarioRepository.existsByEmail(obj.getEmail()))
            throw new ConflictException("E-mail '" + obj.getEmail() + "' já está cadastrado.");

        Usuario salvo = usuarioRepository.save(obj);
        URI location  = URI.create("/usuarios/" + salvo.getId());
        return ResponseEntity.created(location).body(toModel(salvo));
    }

    @Operation(summary = "Atualizar usuário existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> atualizar(@PathVariable Long id,
                                                          @Valid @RequestBody Usuario obj) {
        usuarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        obj.setId(id);
        return ResponseEntity.ok(toModel(usuarioRepository.save(obj)));
    }

    @Operation(summary = "Deletar usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso."),
            @ApiResponse(responseCode = "401", description = "X-API-Key inválida ou ausente."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id))
            throw new ResourceNotFoundException("Usuário", id);
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<Usuario> toModel(Usuario u) {
        return EntityModel.of(u,
                linkTo(methodOn(UsuarioController.class).buscarPorId(u.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).atualizar(u.getId(), u)).withRel("update"),
                linkTo(methodOn(UsuarioController.class).excluir(u.getId())).withRel("delete"));
    }
}
