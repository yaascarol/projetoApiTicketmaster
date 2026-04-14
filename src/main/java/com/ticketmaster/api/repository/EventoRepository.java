package com.ticketmaster.api.repository;

import com.ticketmaster.api.model.Evento;
import com.ticketmaster.api.model.StatusEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    Page<Evento> findByStatus(StatusEvento status, Pageable pageable);
    Page<Evento> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Evento> findByNomeContainingIgnoreCaseAndStatus(String nome, StatusEvento status, Pageable pageable);
}