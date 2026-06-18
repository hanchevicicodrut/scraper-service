package com.chan.scraper_service.repositories;

import com.chan.scraper_service.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

    Optional<Company> findByCompanyIdFromChatbotApiKey(String companyIdFromChatbotApiKey);

    Optional<Company> findByHashedChatbotApiKey(String hashedChatbotApiKey);

    boolean existsByName(String name);
}
