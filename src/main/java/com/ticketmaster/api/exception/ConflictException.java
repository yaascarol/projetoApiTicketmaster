package com.ticketmaster.api.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String mensagem) {
        super(mensagem);
    }
}
