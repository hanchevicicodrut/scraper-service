package com.chan.scraper_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Entity
@Table(name = "company")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "modifiedby", length = 255, nullable = false)
    private String modifiedBy;

    @CreatedDate
    @Column(name = "createdat", nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updatedat", nullable = false)
    private Instant updatedAt;

    @Column(name = "company_id_from_chatbot_api_key", length = 255, nullable = false)
    private String companyIdFromChatbotApiKey;

    @Column(name = "hashed_chatbot_api_key", length = 255, nullable = false)
    private String hashedChatbotApiKey;
}
