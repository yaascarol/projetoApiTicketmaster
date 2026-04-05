package com.seu.evento.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome obrigatório")
    private String nome;

    @Email(message = "Digite um email válido")
    @NotBlank
    @Column(unique = true)
    private String email;

    @Size(min = 8, message = "Digite no mínimo 8 caracteres")
    private String senha;

    private String apiKey;
}