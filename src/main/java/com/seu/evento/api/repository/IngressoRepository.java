package com.seu.evento.api.repository;

import com.seu.evento.api.model.Ingresso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngressoRepository extends JpaRepository<Ingresso, Long> {
}