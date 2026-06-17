package com.example.be.infrastructure.persistence.entity;

import com.example.be.domain.entity.FieldType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "template_fields", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"template_id", "field_key"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateFieldJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ContractTemplateJpaEntity contractTemplate;

    @Column(name = "field_key", nullable = false)
    private String fieldKey;

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false)
    private FieldType fieldType;

    @Column(nullable = false)
    private boolean required;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "default_value")
    private String defaultValue;
}
