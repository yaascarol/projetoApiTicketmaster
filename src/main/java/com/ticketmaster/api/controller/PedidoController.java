package com.ticketmaster.api.controller;

import com.ticketmaster.api.exception.ResourceNotFoundException;
import com.ticketmaster.api.model.Pedido;
import com.ticketmaster.api.model.StatusPedido;
import com.ticketmaster.api.repository.PedidoRepository;
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
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos")
public class PedidoController {

    @Autowired
    private PedidoRepository repository;

    // GET 1 — lista todos
    @Operation(summary = "Listar todos os pedidos (paginado)")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Pedido>>> listar(Pageable pageable) {
        Page<Pedido> page = repository.findAll(pageable);
        List<EntityModel<Pedido>> resources = page.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(PedidoController.class).listar(pageable)).withSelfRel()));
    }

    // GET 2 — por ID
    @Operation(summary = "Buscar pedido por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Pedido>> buscarPorId(@PathVariable Long id) {
        Pedido obj = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
        return ResponseEntity.ok(toModel(obj));
    }

    // GET 3 — consulta personalizada: filtra por usuário e/ou status
    @Operation(summary = "Buscar pedidos por usuário e/ou status (consulta personalizada, paginado)",
            description = "Ambos os parâmetros são opcionais. Informe usuarioId, status, ou os dois juntos.")
    @ApiResponse(responseCode = "200", description = "Pedidos encontrados")
    @GetMapping("/busca")
    public ResponseEntity<Page<Pedido>> buscar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) StatusPedido status,
            Pageable pageable) {
        if (usuarioId != null && status != null) {
            return ResponseEntity.ok(repository.findByUsuarioIdAndStatus(usuarioId, status, pageable));
        }
        if (usuarioId != null) {
            return ResponseEntity.ok(repository.findByUsuarioId(usuarioId, pageable));
        }
        if (status != null) {
            return ResponseEntity.ok(repository.findByStatus(status, pageable));
        }
        return ResponseEntity.ok(repository.findAll(pageable));
    }

    @Operation(summary = "Criar novo pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Pedido>> criar(@Valid @RequestBody Pedido obj) {
        obj.setStatus(StatusPedido.PENDENTE);
        Pedido salvo = repository.save(obj);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(salvo));
    }

    @Operation(summary = "Atualizar pedido existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Pedido>> atualizar(@PathVariable Long id, @Valid @RequestBody Pedido obj) {
        repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
        obj.setId(id);
        return ResponseEntity.ok(toModel(repository.save(obj)));
    }

    @Operation(summary = "Deletar pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Pedido", id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<Pedido> toModel(Pedido pedido) {
        return EntityModel.of(pedido,
                linkTo(methodOn(PedidoController.class).buscarPorId(pedido.getId())).withSelfRel(),
                linkTo(methodOn(PedidoController.class).atualizar(pedido.getId(), pedido)).withRel("update"),
                linkTo(methodOn(PedidoController.class).excluir(pedido.getId())).withRel("delete"));
    }
}