package com.ticketmaster.api.repository;

import com.ticketmaster.api.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    boolean existsByEmail(String email);
}