package com.example.be.infrastructure.persistence.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "contract_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractDataJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", unique = true, nullable = false)
    private ContractJpaEntity contract;

    @Column(name = "data_json", columnDefinition = "TEXT", nullable = false)
    private String dataJson;
}
