package com.ticketmaster.api.infrastructure;

import com.ticketmaster.api.model.*;
import com.ticketmaster.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(
            ArtistaRepository  artistaRepo,
            EventoRepository   eventoRepo,
            IngressoRepository ingressoRepo,
            UsuarioRepository  usuarioRepo) {

        return args -> {

            // ── Artistas ──────────────────────────────────────────────────────
            Artista taylorSwift  = new Artista(); taylorSwift.setNome("Taylor Swift");   taylorSwift.setGenero(GeneroMusical.POP);       taylorSwift.setBio("Maior turnê da história");
            Artista badBunny     = new Artista(); badBunny.setNome("Bad Bunny");          badBunny.setGenero(GeneroMusical.HIP_HOP);      badBunny.setBio("Ícone do reggaeton moderno");
            Artista beyonce      = new Artista(); beyonce.setNome("Beyoncé");             beyonce.setGenero(GeneroMusical.POP);           beyonce.setBio("Queen Bey, Renaissance World Tour");
            Artista coldplay     = new Artista(); coldplay.setNome("Coldplay");           coldplay.setGenero(GeneroMusical.ROCK);         coldplay.setBio("Music of the Spheres Tour");
            Artista anitta       = new Artista(); anitta.setNome("Anitta");               anitta.setGenero(GeneroMusical.FUNK);           anitta.setBio("A garota mais bonita da favela");
            Artista caetanoVeloso= new Artista(); caetanoVeloso.setNome("Caetano Veloso");caetanoVeloso.setGenero(GeneroMusical.MPB);    caetanoVeloso.setBio("Ícone eterno da MPB");

            taylorSwift   = artistaRepo.save(taylorSwift);
            badBunny      = artistaRepo.save(badBunny);
            beyonce       = artistaRepo.save(beyonce);
            coldplay      = artistaRepo.save(coldplay);
            anitta        = artistaRepo.save(anitta);
            caetanoVeloso = artistaRepo.save(caetanoVeloso);
            log.info("Artistas carregados: {}", artistaRepo.count());

            // ── Eventos ───────────────────────────────────────────────────────
            Evento e1 = new Evento();
            e1.setNome("Eras Tour — São Paulo");
            e1.setDescricao("A maior turnê da história em solo brasileiro");
            e1.setLocal("Estádio MorumBIS, São Paulo");
            e1.setDataEvento(LocalDateTime.of(2026, 11, 7, 20, 0));
            e1.setStatus(StatusEvento.CONFIRMADO);
            e1.setArtistas(List.of(taylorSwift));

            Evento e2 = new Evento();
            e2.setNome("Coldplay Music of the Spheres");
            e2.setDescricao("Show sustentável com drones e pulseiras LED");
            e2.setLocal("Nilton Santos, Rio de Janeiro");
            e2.setDataEvento(LocalDateTime.of(2026, 10, 20, 21, 0));
            e2.setStatus(StatusEvento.CONFIRMADO);
            e2.setArtistas(List.of(coldplay));

            Evento e3 = new Evento();
            e3.setNome("Festival Nordestino de MPB");
            e3.setDescricao("Celebração da cultura musical nordestina");
            e3.setLocal("Parque da Cidadania, Fortaleza");
            e3.setDataEvento(LocalDateTime.of(2026, 9, 5, 18, 0));
            e3.setStatus(StatusEvento.PREVISTO);
            e3.setArtistas(List.of(caetanoVeloso));

            Evento e4 = new Evento();
            e4.setNome("Carnaval Eletrônico — Salvador");
            e4.setDescricao("Funk e hip-hop dominando o circuito barra-ondina");
            e4.setLocal("Circuito Barra-Ondina, Salvador");
            e4.setDataEvento(LocalDateTime.of(2027, 2, 28, 22, 0));
            e4.setStatus(StatusEvento.PREVISTO);
            e4.setArtistas(List.of(anitta, badBunny));

            Evento e5 = new Evento();
            e5.setNome("Renaissance World Tour Brasil");
            e5.setDescricao("Beyoncé em Brasília — única data no Centro-Oeste");
            e5.setLocal("Estádio Nacional Mané Garrincha, Brasília");
            e5.setDataEvento(LocalDateTime.of(2026, 8, 15, 20, 30));
            e5.setStatus(StatusEvento.CONFIRMADO);
            e5.setArtistas(List.of(beyonce));

            e1 = eventoRepo.save(e1);
            e2 = eventoRepo.save(e2);
            e3 = eventoRepo.save(e3);
            e4 = eventoRepo.save(e4);
            e5 = eventoRepo.save(e5);
            log.info("Eventos carregados: {}", eventoRepo.count());

            // ── Ingressos ─────────────────────────────────────────────────────
            ingressoRepo.saveAll(List.of(
                    ingresso(e1, TipoIngresso.PISTA,      new BigDecimal("890.00"),   5000),
                    ingresso(e1, TipoIngresso.VIP,        new BigDecimal("2500.00"),   500),
                    ingresso(e1, TipoIngresso.CAMAROTE,   new BigDecimal("4800.00"),   200),
                    ingresso(e2, TipoIngresso.PISTA,      new BigDecimal("650.00"),   8000),
                    ingresso(e2, TipoIngresso.CADEIRA,    new BigDecimal("980.00"),   3000),
                    ingresso(e2, TipoIngresso.VIP,        new BigDecimal("1800.00"),   400),
                    ingresso(e3, TipoIngresso.PISTA,      new BigDecimal("120.00"),   2000),
                    ingresso(e3, TipoIngresso.MEIA_ENTRADA,new BigDecimal("60.00"),  1000),
                    ingresso(e4, TipoIngresso.PISTA,      new BigDecimal("480.00"),  10000),
                    ingresso(e4, TipoIngresso.CAMAROTE,   new BigDecimal("3200.00"),   150),
                    ingresso(e5, TipoIngresso.PISTA,      new BigDecimal("750.00"),   6000),
                    ingresso(e5, TipoIngresso.VIP,        new BigDecimal("2200.00"),   600)
            ));
            log.info("Ingressos carregados: {}", ingressoRepo.count());

            // ── Usuários ──────────────────────────────────────────────────────
            Usuario u1 = new Usuario(); u1.setNome("Ana Lima");   u1.setEmail("ana.lima@email.com");
            Usuario u2 = new Usuario(); u2.setNome("Bruno Melo"); u2.setEmail("bruno.melo@email.com");
            Usuario u3 = new Usuario(); u3.setNome("Carla Dias"); u3.setEmail("carla.dias@email.com");

            usuarioRepo.saveAll(List.of(u1, u2, u3));
            log.info("Usuários carregados: {}", usuarioRepo.count());

            log.info("=== Ticketmaster API iniciada com sucesso! Acesse /swagger-ui.html ===");
        };
    }

    private Ingresso ingresso(Evento evento, TipoIngresso tipo, BigDecimal preco, int quantidade) {
        Ingresso i = new Ingresso();
        i.setEvento(evento);
        i.setTipo(tipo);
        i.setPreco(preco);
        i.setQuantidadeDisponivel(quantidade);
        return i;
    }
}
