package com.seu.evento.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(exclude = {"pedido", "ingresso"})
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade mínima é 1")
    private Integer quantidade;

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "ingresso_id", nullable = false)
    private Ingresso ingresso;
}
