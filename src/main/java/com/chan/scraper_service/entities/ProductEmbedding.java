package com.chan.scraper_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_embeddings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // The actual vector — provider agnostic
    // pgvector: "vector(1536)" for OpenAI
    // pgvector: "vector(768)"  for most local models
    @Column(columnDefinition = "text")  // stored as text until you pick provider
    private String vectorJson;          // "[0.23, 0.87, ...]"

    // What model generated this
    private String modelName;           // "text-embedding-3-small"
    private String modelProvider;       // "openai" / "ollama" / "spring-ai"
    private Integer dimensions;         // 1536 / 768 / 384

    // What text was embedded (crucial for debugging + re-embedding)
    @Column(columnDefinition = "text")
    private String embeddedText;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
