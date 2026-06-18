package com.example.be.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractSnapshot {
    private Long id;
    private Long contractId;
    private String renderedHtml;
    private String docxPath;
    private String pdfPath;
    private OffsetDateTime createdAt;
}
