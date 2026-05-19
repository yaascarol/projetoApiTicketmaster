package com.ticketmaster.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class IdempotencyRecord {

    @Id
    private String chave;

    private int statusHttp;

    @Column(columnDefinition = "TEXT")
    private String corpoResposta;

    private LocalDateTime criadoEm;
}
