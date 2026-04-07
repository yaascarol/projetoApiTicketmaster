package com.seu.evento.api.repository;

import com.seu.evento.api.model.ItemPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
    Page<ItemPedido> findByPedidoId(Long pedidoId, Pageable pageable);
}
