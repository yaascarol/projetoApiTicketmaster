package com.ticketmaster.api.repository;

import com.ticketmaster.api.model.Artista;
import com.ticketmaster.api.model.GeneroMusical;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistaRepository extends JpaRepository<Artista, Long> {
    Page<Artista> findByGenero(GeneroMusical genero, Pageable pageable);
    Page<Artista> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}