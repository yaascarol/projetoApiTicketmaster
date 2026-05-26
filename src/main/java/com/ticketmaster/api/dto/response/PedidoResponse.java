package com.ticketmaster.api.dto.response;

import com.ticketmaster.api.model.StatusPedido;
import org.springframework.hateoas.RepresentationModel;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
public class PedidoResponse extends RepresentationModel<PedidoResponse> {
    private Long id;
    private StatusPedido status;
    private LocalDateTime dataPedido;
    private Long usuarioId;
    private String nomeUsuario;
    private int totalItens;
}
