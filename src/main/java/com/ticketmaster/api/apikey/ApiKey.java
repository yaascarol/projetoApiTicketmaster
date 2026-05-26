package com.ticketmaster.api.apikey;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_api_keys")
@Getter @Setter @NoArgsConstructor
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String keyValue;

    @Column(nullable = false, length = 100)
    private String owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccessLevel accessLevel = AccessLevel.WRITE;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.keyValue  = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public enum AccessLevel { READ, WRITE, ADMIN }
}
