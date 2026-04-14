package com.ticketmaster.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @Positive(message = "O ID não pode ser negativo")
    private Long id;

    @NotBlank(message = "Informe o nome do evento")
    private String nome;

    private String descricao;

    @NotNull(message = "Informe a data do evento")
    private LocalDateTime dataEvento;

    @NotBlank(message = "Informe o local do evento")
    private String local;

    @NotNull(message = "Status do evento é obrigatório")
    @Enumerated(EnumType.STRING)
    private StatusEvento status;


    //many-to-many: evento é dono da relação (Jointable)
    @ManyToMany
    @JoinTable(
            name = "evento_artista",
            joinColumns = @JoinColumn(name = "evento_id"),
            inverseJoinColumns = @JoinColumn(name = "artista_id")
    )
    private List<Artista> artistas;

    //one-to-many: um evento tem varios ingressos
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Ingresso> ingressos;
}