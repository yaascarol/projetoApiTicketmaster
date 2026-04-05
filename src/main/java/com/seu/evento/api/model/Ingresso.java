package com.seu.evento.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class Ingresso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoIngresso tipo;

    @Positive(message = "O preço deve ser maior que zero")
    private BigDecimal preco;

    @Min(0)
    private Integer quantidadeEstoque;

    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;
}