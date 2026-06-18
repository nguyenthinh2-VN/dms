package com.example.be.infrastructure.persistence.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "contract_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractSnapshotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private ContractJpaEntity contract;

    @Column(name = "rendered_html", columnDefinition = "TEXT")
    private String renderedHtml;

    @Column(name = "docx_path")
    private String docxPath;

    @Column(name = "pdf_path")
    private String pdfPath;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
