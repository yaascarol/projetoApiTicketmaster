package com.ticketmaster.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //não envia ao body
    private LocalDateTime dataPedido;

    @NotNull(message = "O status do pedido é obrigatório")
    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    //many-to-one: vários pedidos podem pertencer ao mesmo usuário
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull(message = "O usuário é obrigatório")
    private Usuario usuario;

    //o-t-m: um pedido tem vários itens
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();
}