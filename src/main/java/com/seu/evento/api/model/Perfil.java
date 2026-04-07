package com.seu.evento.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bio;
    
    private String redeSocial;

    @OneToOne(mappedBy = "perfil")
    private Usuario usuario;
}
