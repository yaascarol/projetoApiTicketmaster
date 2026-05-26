package com.ticketmaster.api.dto.request;

import com.ticketmaster.api.model.StatusEvento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class EventoRequest {

    @NotBlank(message = "Informe o nome do evento")
    private String nome;

    private String descricao;

    @NotNull(message = "Informe a data do evento")
    private LocalDateTime dataEvento;

    @NotBlank(message = "Informe o local do evento")
    private String local;

    @NotNull(message = "Status do evento é obrigatório")
    private StatusEvento status;

    private List<Long> artistaIds;
}
