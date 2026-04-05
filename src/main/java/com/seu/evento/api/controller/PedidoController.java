package com.seu.evento.api.controller;

import com.seu.evento.api.model.Pedido;
import com.seu.evento.api.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/pedidos")
public class PedidoController {

    @Autowired
    private PedidoRepository repository;

    @PostMapping
    public Pedido criar(@RequestBody Pedido pedido) {
        if (pedido.getDataPedido() == null) {
            pedido.setDataPedido(java.time.LocalDateTime.now());
        }
        return repository.save(pedido);
    }

    @GetMapping
    public List<Pedido> listar() {
        return repository.findAll();
    }

    @PutMapping("/{id}")
    public Pedido atualizarStatus(@PathVariable Long id, @RequestBody Pedido dadosAtualizados) {
        return repository.findById(id)
                .map(pedido -> {
                    pedido.setStatus(dadosAtualizados.getStatus());
                    return repository.save(pedido);
                })
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        repository.deleteById(id);
    }
}