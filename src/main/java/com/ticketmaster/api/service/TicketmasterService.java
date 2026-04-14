package com.ticketmaster.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TicketmasterService {

    // Chave lida do application.properties — nunca hardcoded no código
    @Value("${ticketmaster.api.key}")
    private String apiKey;

    public String buscarEventos() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://app.ticketmaster.com/discovery/v2/events.json?apikey=" + apiKey;
        return restTemplate.getForObject(url, String.class);
    }
}