package com.ticketmaster.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventoResumoDTO {
    private Long id;
    private String nome;
    private String local;
    private String dataEvento;
    private String status;
    private String versaoApi;
}
