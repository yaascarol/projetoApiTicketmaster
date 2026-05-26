package com.ticketmaster.api.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia buckets de rate limiting por IP.
 *
 * Limites (por IP):
 *   GET                       → 30 requisições por minuto
 *   POST / PUT / DELETE       → 10 requisições por minuto
 */
@Configuration
public class RateLimitConfig {

    private static final int  READ_CAPACITY    = 10;
    private static final long READ_REFILL_SECS = 60;

    private static final int  WRITE_CAPACITY    = 5;
    private static final long WRITE_REFILL_SECS = 60;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ip, boolean isWrite) {
        String key = ip + ":" + (isWrite ? "W" : "R");
        return buckets.computeIfAbsent(key, k -> buildBucket(isWrite));
    }

    private Bucket buildBucket(boolean write) {
        Bandwidth limit = write
                ? Bandwidth.builder()
                        .capacity(WRITE_CAPACITY)
                        .refillGreedy(WRITE_CAPACITY, Duration.ofSeconds(WRITE_REFILL_SECS))
                        .build()
                : Bandwidth.builder()
                        .capacity(READ_CAPACITY)
                        .refillGreedy(READ_CAPACITY, Duration.ofSeconds(READ_REFILL_SECS))
                        .build();

        return Bucket.builder().addLimit(limit).build();
    }
}
