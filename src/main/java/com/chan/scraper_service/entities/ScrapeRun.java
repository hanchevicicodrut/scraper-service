package com.chan.scraper_service.entities;

import com.chan.scraper_service.enums.ScrapeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "scrape_runs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ScrapeRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;

            private String sourceWebsite;        // "sportguru.ro"
            private String scrapeUrl;            // the URL scraped

            @Enumerated(EnumType.STRING)
            private ScrapeStatus status;         // RUNNING, SUCCESS, FAILED, PARTIAL

            private Instant startedAt;
            private Instant finishedAt;

            // --- STATISTICS ---
            private Integer totalFound;          // products found on page
            private Integer totalInserted;       // new products added
            private Integer totalUpdated;        // existing products updated
            private Integer totalUnchanged;      // no changes detected
            private Integer totalFailed;         // failed to parse

            // --- ERROR TRACKING ---
            @Column(columnDefinition = "text")
            private String errorMessage;         // if FAILED or PARTIAL

            @CreatedDate
            private Instant createdAt;

            // --- RELATIONSHIP ---
            @OneToMany(mappedBy = "scrapeRun",
                    cascade = CascadeType.ALL,
                    fetch = FetchType.LAZY)
            private List<ProductPriceHistory> priceHistories;
}
