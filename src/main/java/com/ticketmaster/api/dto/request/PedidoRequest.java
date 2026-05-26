package com.ticketmaster.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PedidoRequest {

    @NotNull(message = "O usuário é obrigatório")
    private Long usuarioId;
}
