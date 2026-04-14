package com.seu.evento.api.repository;

import com.seu.evento.api.model.Ingresso;
import com.seu.evento.api.model.TipoIngresso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;

@Repository
public interface IngressoRepository extends JpaRepository<Ingresso, Long> {

    Page<Ingresso> findByPrecoLessThanEqual(BigDecimal preco, Pageable pageable);
    Page<Ingresso> findByTipo(TipoIngresso tipo, Pageable pageable);
}
