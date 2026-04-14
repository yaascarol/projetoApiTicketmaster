package com.seu.evento.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
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
    private List<Ingresso> ingressos;
}
