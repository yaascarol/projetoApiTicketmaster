package com.ticketmaster.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Positive(message = "O ID não pode ser negativo")
    private Long id;

    private String bio;
    
    private String redeSocial;

    @OneToOne(mappedBy = "perfil")
    private Usuario usuario;
}
