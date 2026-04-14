package com.ticketmaster.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
public class Ingresso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O tipo de ingresso é obrigatório")
    @Enumerated(EnumType.STRING)
    private TipoIngresso tipo;

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    private BigDecimal preco;

    @NotNull(message = "A quantidade disponível é obrigatória")
    @Min(value = 0, message = "A quantidade não pode ser negativa")
    private Integer quantidadeDisponivel;

    // Many-to-One: vários ingressos pertencem a um evento
    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    @NotNull(message = "O evento é obrigatório")
    private Evento evento;

    // One-to-Many: um ingresso pode estar em vários itens de pedido
    @OneToMany(mappedBy = "ingresso", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ItemPedido> itensPedido;
}