package com.ticketmaster.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String bio;

    private String cidade;

    private String estado;

    private String urlFoto;

    //evitar loop infinito no json
    @OneToMany(mappedBy = "perfil")
    @JsonIgnore
    private Usuario usuario;
}
