package com.ticketmaster.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade mínima é 1")
    private Integer quantidade;

    // Many-to-One: vários itens pertencem a um pedido
    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    @JsonIgnore
    private Pedido pedido;

    // Many-to-One: vários itens podem referenciar o mesmo ingresso
    @ManyToOne
    @JoinColumn(name = "ingresso_id", nullable = false)
    @NotNull(message = "O ingresso é obrigatório")
    private Ingresso ingresso;
}