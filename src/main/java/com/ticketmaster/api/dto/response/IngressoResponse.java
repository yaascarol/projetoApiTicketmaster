package com.ticketmaster.api.dto.response;

import com.ticketmaster.api.model.TipoIngresso;
import org.springframework.hateoas.RepresentationModel;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class IngressoResponse extends RepresentationModel<IngressoResponse> {
    private Long id;
    private TipoIngresso tipo;
    private BigDecimal preco;
    private Integer quantidadeDisponivel;
    private Long eventoId;
    private String nomeEvento;
}
