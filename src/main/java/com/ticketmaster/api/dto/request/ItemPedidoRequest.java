package com.ticketmaster.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ItemPedidoRequest {

    @NotNull(message = "O pedido é obrigatório")
    private Long pedidoId;

    @NotNull(message = "O ingresso é obrigatório")
    private Long ingressoId;

    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade mínima é 1")
    private Integer quantidade;
}
