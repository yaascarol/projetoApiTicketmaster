package com.seu.evento.api.controller;

import com.seu.evento.api.model.Ingresso;
import com.seu.evento.api.repository.IngressoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/ingressos")
public class IngressoController {

    @Autowired
    private IngressoRepository repository;

    @PostMapping
    public Ingresso criar(@RequestBody Ingresso ingresso) {
        return repository.save(ingresso);
    }

    @GetMapping
    public List<Ingresso> listar() {
        return repository.findAll();
    }

    @PutMapping("/{id}")
    public Ingresso atualizar(@PathVariable Long id, @RequestBody Ingresso dadosAtualizados) {
        return repository.findById(id)
                .map(ingresso -> {
                    ingresso.setTipo(dadosAtualizados.getTipo());
                    ingresso.setPreco(dadosAtualizados.getPreco());
                    ingresso.setQuantidadeEstoque(dadosAtualizados.getQuantidadeEstoque());
                    return repository.save(ingresso);
                })
                .orElseThrow(() -> new RuntimeException("Ingresso não encontrado"));
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        repository.deleteById(id);
    }
}