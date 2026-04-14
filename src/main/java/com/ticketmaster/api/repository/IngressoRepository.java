package com.ticketmaster.api.repository;

import com.ticketmaster.api.model.Ingresso;
import com.ticketmaster.api.model.TipoIngresso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface IngressoRepository extends JpaRepository<Ingresso, Long> {
    Page<Ingresso> findByTipo(TipoIngresso tipo, Pageable pageable);
    Page<Ingresso> findByPrecoLessThanEqual(BigDecimal precoMax, Pageable pageable);
    Page<Ingresso> findByTipoAndPrecoLessThanEqual(TipoIngresso tipo, BigDecimal precoMax, Pageable pageable);
    Page<Ingresso> findByEventoId(Long eventoId, Pageable pageable);
}