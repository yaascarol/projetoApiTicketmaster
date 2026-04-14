package com.ticketmaster.api.controller;

import com.ticketmaster.api.service.TicketmasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/eventos")
public class EventoController {

    @Autowired
    private TicketmasterService service;

    @GetMapping("/externos")
    public String listarEventosDaTicketmaster() {
        return service.buscarEventos();
    }
}