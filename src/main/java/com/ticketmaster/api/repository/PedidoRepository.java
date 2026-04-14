package com.ticketmaster.api.repository;

import com.ticketmaster.api.model.Pedido;
import com.ticketmaster.api.model.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Page<Pedido> findByUsuarioId(Long usuarioId, Pageable pageable);
    Page<Pedido> findByStatus(StatusPedido status, Pageable pageable);
    Page<Pedido> findByUsuarioIdAndStatus(Long usuarioId, StatusPedido status, Pageable pageable);
}