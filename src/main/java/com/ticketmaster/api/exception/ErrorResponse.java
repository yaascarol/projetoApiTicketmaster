package com.ticketmaster.api.exception;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String erro;
    private final String mensagem;
    private final String path;
    private final List<FieldErrorDetail> campos;

    @Getter
    @Builder
    public static class FieldErrorDetail {
        private final String campo;
        private final String mensagem;
    }
}
