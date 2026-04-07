package com.seu.evento.api.repository;

import com.seu.evento.api.model.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    Page<Evento> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
