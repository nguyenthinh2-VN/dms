package com.example.be.application.service;

import com.example.be.domain.repository.ContractRepository;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Service
public class ContractNumberGenerator {

    private final ContractRepository contractRepository;

    public ContractNumberGenerator(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    public String generate(String templateCode, String creatorName) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyy"));
        String cleanName = sanitizeName(creatorName);
        
        String baseNo = "HD_" + templateCode + "_" + dateStr + "_" + cleanName;
        String contractNo = baseNo;
        int count = 2;
        
        while (contractRepository.existsByContractNo(contractNo)) {
            contractNo = baseNo + "_" + count;
            count++;
        }
        
        return contractNo;
    }

    private String sanitizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Unknown";
        }
        // Normalize unicode characters to NFD form, e.g. "Nguyễn" -> "Nguye~n"
        String nfdNormalizedString = Normalizer.normalize(name, Normalizer.Form.NFD);
        // Remove diacritical marks
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String sanitized = pattern.matcher(nfdNormalizedString).replaceAll("");
        // Replace 'đ' and 'Đ'
        sanitized = sanitized.replace('đ', 'd').replace('Đ', 'D');
        // Replace spaces with underscore and remove non-alphanumeric chars
        sanitized = sanitized.replaceAll("\\s+", "_");
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9_]", "");
        return sanitized;
    }
}
