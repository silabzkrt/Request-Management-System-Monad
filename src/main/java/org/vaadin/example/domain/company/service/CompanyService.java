package org.vaadin.example.domain.company.service;

import org.springframework.stereotype.Service;
import org.vaadin.example.domain.company.dao.CompanyRepository;
import org.vaadin.example.domain.company.model.Company;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    public Optional<Company> findById(Long id) {
        return companyRepository.findById(id);
    }

    public Optional<Company> findByCompanyName(String name) {
        return companyRepository.findByCompanyName(name);
    }

    public Company save(Company company) {
        return companyRepository.save(company);
    }

    public void delete(Long id) {
        companyRepository.deleteById(id);
    }
}
