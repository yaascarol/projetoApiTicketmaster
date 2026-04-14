package com.ticketmaster.api.controller;

import com.ticketmaster.api.model.ItemPedido;
import com.ticketmaster.api.repository.ItemPedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/itens-pedido")
public class ItemPedidoController {

    @Autowired
    private ItemPedidoRepository repository;

    @PostMapping
    public ItemPedido adicionar(@RequestBody ItemPedido item) {
        return repository.save(item);
    }

    @GetMapping
    public List<ItemPedido> listar() {
        return repository.findAll();
    }

    @PutMapping("/{id}")
    public ItemPedido atualizarQuantidade(@PathVariable Long id, @RequestBody ItemPedido dados) {
        return repository.findById(id)
                .map(item -> {
                    item.setQuantidade(dados.getQuantidade());
                    return repository.save(item);
                })
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        repository.deleteById(id);
    }
}