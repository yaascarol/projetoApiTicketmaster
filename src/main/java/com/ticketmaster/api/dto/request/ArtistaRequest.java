package com.ticketmaster.api.dto.request;

import com.ticketmaster.api.model.GeneroMusical;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ArtistaRequest {

    @NotBlank(message = "Nome do artista é obrigatório")
    private String nome;

    @NotNull(message = "Informe um gênero musical")
    private GeneroMusical genero;

    private String bio;
    private String urlImagem;
}
