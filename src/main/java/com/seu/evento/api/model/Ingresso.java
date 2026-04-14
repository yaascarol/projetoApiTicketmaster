package com.seu.evento.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@ToString(exclude = "evento")
public class Ingresso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O tipo de ingresso é obrigatório")
    private TipoIngresso tipo;

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    private BigDecimal preco;

    @NotNull(message = "A quantidade é obrigatória")
    @Min(0)
    private Integer quantidadeEstoque;

    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;
}
