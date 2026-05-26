package com.ticketmaster.api.dto.response;

import com.ticketmaster.api.model.StatusEvento;
import org.springframework.hateoas.RepresentationModel;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class EventoResponse extends RepresentationModel<EventoResponse> {
    private Long id;
    private String nome;
    private String descricao;
    private LocalDateTime dataEvento;
    private String local;
    private StatusEvento status;
    private List<String> artistas;
    private int totalIngressos;
}
