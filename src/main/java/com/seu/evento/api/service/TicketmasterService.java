package com.seu.evento.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TicketmasterService {

    private final String API_KEY = "vm4lEvubTEujseI2pY6GZk77Tn5gH8Y7";

    public String buscarEventos() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://app.ticketmaster.com/discovery/v2/events.json?apikey=" + API_KEY;

        return restTemplate.getForObject(url, String.class);
    }
}