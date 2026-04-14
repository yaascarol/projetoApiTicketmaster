package com.ticketmaster.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Artista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do artista é obrigatório")
    private String nome;

    @NotNull(message = "Informe um gênero musical")
    @Enumerated(EnumType.STRING)
    private GeneroMusical genero;

    private String bio;

    private String urlImagem;

    //many-to-many: evento é o dono da relaçao

    @ManyToMany(mappedBy = "artistas")
    @JsonIgnore
    private List<Evento> eventos;
}
