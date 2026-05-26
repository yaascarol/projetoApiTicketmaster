package com.ticketmaster.api.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " com ID " + id + " não encontrado(a).");
    }
    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }
}
