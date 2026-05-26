package com.ticketmaster.api.apikey;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ApiKeyRequest {

    @NotBlank(message = "O nome do responsável é obrigatório")
    @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres")
    private String owner;

    private ApiKey.AccessLevel accessLevel;
}
