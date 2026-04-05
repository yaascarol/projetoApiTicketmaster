package com.seu.evento.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String ticketmasterId;

    @NotBlank
    private String nome;

    private LocalDateTime dataEvento;

    private String local;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL)
    private java.util.List<Ingresso> ingressos;
}