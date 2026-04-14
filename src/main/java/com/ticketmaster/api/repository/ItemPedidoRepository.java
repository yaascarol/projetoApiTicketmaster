package com.ticketmaster.api.repository;

import com.ticketmaster.api.model.ItemPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
    Page<ItemPedido> findByPedidoId(Long pedidoId, Pageable pageable);
    Page<ItemPedido> findByIngressoId(Long ingressoId, Pageable pageable);
}