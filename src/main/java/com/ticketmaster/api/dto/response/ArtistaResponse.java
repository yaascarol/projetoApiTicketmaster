package com.ticketmaster.api.dto.response;

import com.ticketmaster.api.model.GeneroMusical;
import org.springframework.hateoas.RepresentationModel;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ArtistaResponse extends RepresentationModel<ArtistaResponse> {
    private Long id;
    private String nome;
    private GeneroMusical genero;
    private String bio;
    private String urlImagem;
    private int totalEventos;
}
