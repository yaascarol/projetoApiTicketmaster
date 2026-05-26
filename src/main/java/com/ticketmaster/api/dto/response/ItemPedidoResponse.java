package com.ticketmaster.api.dto.response;

import com.ticketmaster.api.model.TipoIngresso;
import org.springframework.hateoas.RepresentationModel;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class ItemPedidoResponse extends RepresentationModel<ItemPedidoResponse> {
    private Long id;
    private Integer quantidade;
    private Long ingressoId;
    private TipoIngresso tipoIngresso;
    private BigDecimal precoUnitario;
    private Long pedidoId;
}
