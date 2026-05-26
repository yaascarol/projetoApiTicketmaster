package com.ticketmaster.api.apikey;

import java.time.LocalDateTime;

public record ApiKeyResponse(
        Long id,
        String apiKey,
        String owner,
        ApiKey.AccessLevel accessLevel,
        boolean active,
        LocalDateTime createdAt
) {}
