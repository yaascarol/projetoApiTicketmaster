package com.seu.evento.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter // Melhor que @Data para evitar loops
@Setter
@ToString(exclude = {"ingressos", "categorias"})
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

    @ManyToMany
    @JoinTable(
        name = "evento_categoria",
        joinColumns = @JoinColumn(name = "evento_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private List<Categoria> categorias;
}
