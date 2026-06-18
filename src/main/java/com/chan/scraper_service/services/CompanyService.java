package com.chan.scraper_service.services;

import com.chan.scraper_service.entities.Company;
import com.chan.scraper_service.repositories.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    // ── CREATE ────────────────────────────────────────────────────
    @Transactional
    public Company create(Company company) {
        if (companyRepository.existsByName(company.getName())) {
            throw new IllegalArgumentException(
                    "Company already exists with name: " + company.getName());
        }

        Company saved = companyRepository.save(company);
        log.info("➕ Created company: [{}] {}", saved.getId(), saved.getName());
        return saved;
    }

    // ── READ ──────────────────────────────────────────────────────
    public Company findById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Company not found with id: " + id));
    }

    public Optional<Company> findByName(String name) {
        return companyRepository.findByName(name);
    }

    public Optional<Company> findByCompanyIdFromChatbotApiKey(String companyIdFromChatbotApiKey) {
        return companyRepository.findByCompanyIdFromChatbotApiKey(companyIdFromChatbotApiKey);
    }

    public Optional<Company> findByHashedChatbotApiKey(String hashedChatbotApiKey) {
        return companyRepository.findByHashedChatbotApiKey(hashedChatbotApiKey);
    }

    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    // ── UPDATE ────────────────────────────────────────────────────
    @Transactional
    public Company update(Long id, Company updatedData, String modifiedBy) {
        Company existing = findById(id);

        existing.setName(updatedData.getName());
        existing.setCompanyIdFromChatbotApiKey(updatedData.getCompanyIdFromChatbotApiKey());
        existing.setHashedChatbotApiKey(updatedData.getHashedChatbotApiKey());
        existing.setModifiedBy(modifiedBy);

        Company saved = companyRepository.save(existing);
        log.info("🔄 Updated company: [{}] {} by {}", saved.getId(), saved.getName(), modifiedBy);
        return saved;
    }

    // ── DELETE ────────────────────────────────────────────────────
    @Transactional
    public void delete(Long id) {
        Company existing = findById(id);
        companyRepository.delete(existing);
        log.info("🗑️ Deleted company: [{}] {}", existing.getId(), existing.getName());
    }
}
