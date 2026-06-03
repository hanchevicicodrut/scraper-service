package com.chan.scraper_service.repositories;

import com.chan.scraper_service.entities.ScrapeRun;
import com.chan.scraper_service.enums.ScrapeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapeRunRepository
        extends JpaRepository<ScrapeRun, Long> {

    // ── LATEST RUN ────────────────────────────────────────────────
    Optional<ScrapeRun> findTopBySourceWebsiteOrderByCreatedAtDesc(
            String sourceWebsite);

    Optional<ScrapeRun> findTopByOrderByCreatedAtDesc();

    // ── BY STATUS ─────────────────────────────────────────────────
    List<ScrapeRun> findByStatus(ScrapeStatus status);

    List<ScrapeRun> findBySourceWebsiteOrderByCreatedAtDesc(
            String sourceWebsite);

    // ── HISTORY ───────────────────────────────────────────────────
    List<ScrapeRun> findByCreatedAtAfter(LocalDateTime after);

    // ── STATS ─────────────────────────────────────────────────────
    @Query("""
        SELECT SUM(r.totalInserted) FROM ScrapeRun r
        WHERE r.sourceWebsite = :sourceWebsite
        """)
    Long sumTotalInsertedBySourceWebsite(
            @Param("sourceWebsite") String sourceWebsite);

    long countByStatus(ScrapeStatus status);
}
