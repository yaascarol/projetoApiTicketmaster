package com.ticketmaster.api.dto.request;

import com.ticketmaster.api.model.TipoIngresso;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class IngressoRequest {

    @NotNull(message = "O evento é obrigatório")
    private Long eventoId;

    @NotNull(message = "O tipo de ingresso é obrigatório")
    private TipoIngresso tipo;

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    private BigDecimal preco;

    @NotNull(message = "A quantidade disponível é obrigatória")
    @Min(value = 0, message = "A quantidade não pode ser negativa")
    private Integer quantidadeDisponivel;
}
