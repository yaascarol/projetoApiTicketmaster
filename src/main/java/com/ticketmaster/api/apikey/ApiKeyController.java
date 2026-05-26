package com.ticketmaster.api.apikey;

import com.ticketmaster.api.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/auth/api-keys")
@RequiredArgsConstructor
@Tag(
        name = "Autenticação — API Keys",
        description = "Geração e gerenciamento das chaves de acesso para proteger os endpoints da Ticketmaster API."
)
public class ApiKeyController {

    private final ApiKeyRepository apiKeyRepository;

    @Operation(
            summary = "Gerar nova chave de API",
            description = """
                    Gera uma nova chave de acesso para utilizar nos endpoints protegidos.
                    Use o valor retornado no campo `apiKey` dentro do header `X-API-Key`.

                    Níveis de acesso:
                    READ  → permite apenas consultas GET
                    WRITE → permite consultas e alterações (padrão)
                    ADMIN → acesso completo incluindo revogação de chaves
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chave gerada com sucesso.",
                    headers = @Header(name = "Location", description = "URI da nova chave criada")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos.",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas. Aguarde antes de tentar novamente.")
    })
    @PostMapping
    public ResponseEntity<ApiKeyResponse> generate(@Valid @RequestBody ApiKeyRequest req) {
        ApiKey entity = new ApiKey();
        entity.setOwner(req.getOwner());
        if (req.getAccessLevel() != null) entity.setAccessLevel(req.getAccessLevel());

        ApiKey saved = apiKeyRepository.save(entity);
        return ResponseEntity
                .created(URI.create("/api/auth/api-keys/" + saved.getId()))
                .body(toResponse(saved));
    }

    @Operation(summary = "Listar todas as chaves de API",
               description = "Retorna todas as chaves cadastradas, ativas e inativas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso."),
            @ApiResponse(responseCode = "401", description = "Acesso não autorizado.")
    })
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listAll() {
        return ResponseEntity.ok(
                apiKeyRepository.findAll().stream().map(this::toResponse).toList()
        );
    }

    @Operation(summary = "Buscar chave de API por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chave encontrada."),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiKeyResponse> getById(
            @Parameter(description = "ID da chave de API", example = "1")
            @PathVariable Long id) {
        return apiKeyRepository.findById(id)
                .map(k -> ResponseEntity.ok(toResponse(k)))
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey", id));
    }

    @Operation(
            summary = "Revogar uma chave de API",
            description = "Desativa a chave. Requisições usando essa chave passarão a receber 401."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chave revogada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiKeyResponse> revoke(
            @Parameter(description = "ID da chave a revogar", example = "1")
            @PathVariable Long id) {
        ApiKey key = apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey", id));
        key.setActive(false);
        return ResponseEntity.ok(toResponse(apiKeyRepository.save(key)));
    }

    private ApiKeyResponse toResponse(ApiKey k) {
        return new ApiKeyResponse(k.getId(), k.getKeyValue(), k.getOwner(),
                k.getAccessLevel(), k.isActive(), k.getCreatedAt());
    }
}
