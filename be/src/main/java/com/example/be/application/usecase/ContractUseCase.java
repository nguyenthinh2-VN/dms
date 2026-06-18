package com.example.be.application.usecase;

import com.example.be.application.dto.contract.*;
import com.example.be.application.service.*;
import com.example.be.domain.entity.*;
import com.example.be.domain.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContractUseCase {

    private final ContractRepository contractRepository;
    private final ContractDataRepository contractDataRepository;
    private final ContractSnapshotRepository contractSnapshotRepository;
    private final ContractTemplateRepository templateRepository;
    private final TemplateFieldRepository templateFieldRepository;
    private final ContractFileStorage fileStorage;
    private final UserRepository userRepository;
    
    private final DocxMergeService mergeService;
    private final DocxToHtmlConverter htmlConverter;
    private final DocxToPdfConverter pdfConverter;
    private final FieldDataValidator validator;
    private final ContractNumberGenerator numberGenerator;
    private final PermissionChecker permissionChecker;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContractUseCase(ContractRepository contractRepository,
                           ContractDataRepository contractDataRepository,
                           ContractSnapshotRepository contractSnapshotRepository,
                           ContractTemplateRepository templateRepository,
                           TemplateFieldRepository templateFieldRepository,
                           ContractFileStorage fileStorage,
                           UserRepository userRepository,
                           DocxMergeService mergeService,
                           DocxToHtmlConverter htmlConverter,
                           DocxToPdfConverter pdfConverter,
                           FieldDataValidator validator,
                           ContractNumberGenerator numberGenerator,
                           PermissionChecker permissionChecker) {
        this.contractRepository = contractRepository;
        this.contractDataRepository = contractDataRepository;
        this.contractSnapshotRepository = contractSnapshotRepository;
        this.templateRepository = templateRepository;
        this.templateFieldRepository = templateFieldRepository;
        this.fileStorage = fileStorage;
        this.userRepository = userRepository;
        this.mergeService = mergeService;
        this.htmlConverter = htmlConverter;
        this.pdfConverter = pdfConverter;
        this.validator = validator;
        this.numberGenerator = numberGenerator;
        this.permissionChecker = permissionChecker;
    }

    @Transactional
    public ContractResponse create(CreateContractRequest request, Long currentUserId, String creatorName) throws Exception {
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        permissionChecker.requirePermission(user, "contract.create");
        
        ContractTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("Template không tồn tại"));
                
        if (template.getStatus() != TemplateStatus.ACTIVE) {
            throw new IllegalArgumentException("Template không còn ACTIVE");
        }

        template.setFields(templateFieldRepository.findByTemplateId(template.getId()));
        validator.validate(template.getFields(), request.getData());
        
        // Scope vụ việc: tạm thời bypass nếu chưa có CaseAccessChecker
        // TODO: verify legal case permission
        
        String contractNo = numberGenerator.generate(template.getCode(), creatorName);
        
        byte[] templateDocx = fileStorage.load(template.getStoragePath());
        byte[] mergedDocx = mergeService.merge(templateDocx, request.getData());
        
        // Save DOCX and PDF to storage
        String docxPath = "contracts/" + contractNo + ".docx";
        fileStorage.store(mergedDocx, docxPath);
        
        byte[] pdfBytes = pdfConverter.convertToPdf(mergedDocx);
        String pdfPath = "contracts/" + contractNo + ".pdf";
        fileStorage.store(pdfBytes, pdfPath);
        
        org.docx4j.openpackaging.packages.WordprocessingMLPackage wordMLPackage;
        try (java.io.ByteArrayInputStream is = new java.io.ByteArrayInputStream(mergedDocx)) {
            wordMLPackage = org.docx4j.openpackaging.packages.WordprocessingMLPackage.load(is);
        }
        String html = htmlConverter.convertToHtml(wordMLPackage);
        
        Contract contract = new Contract();
        contract.setContractNo(contractNo);
        contract.setTemplateId(template.getId());
        contract.setTemplateVersion(template.getVersion());
        contract.setLegalCaseId(request.getLegalCaseId());
        contract.setStatus(ContractStatus.FINALIZED);
        contract.setCreatedBy(currentUserId);
        
        Contract savedContract = contractRepository.save(contract);
        
        ContractData contractData = new ContractData();
        contractData.setContractId(savedContract.getId());
        contractData.setDataJson(objectMapper.writeValueAsString(request.getData()));
        contractDataRepository.save(contractData);
        
        ContractSnapshot snapshot = new ContractSnapshot();
        snapshot.setContractId(savedContract.getId());
        snapshot.setRenderedHtml(html);
        snapshot.setDocxPath(docxPath);
        snapshot.setPdfPath(pdfPath);
        contractSnapshotRepository.save(snapshot);
        
        return buildContractResponse(savedContract, request.getData(), snapshot);
    }
    
    public ContractResponse getById(Long id, Long currentUserId) throws Exception {
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        permissionChecker.requirePermission(user, "contract.view");
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hợp đồng không tồn tại"));
        
        ContractData data = contractDataRepository.findByContractId(id).orElse(null);
        ContractSnapshot snapshot = contractSnapshotRepository.findByContractId(id).orElse(null);
        
        java.util.Map<String, String> dataMap = new java.util.HashMap<>();
        if (data != null && data.getDataJson() != null) {
            dataMap = objectMapper.readValue(data.getDataJson(), new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>(){});
        }
        
        return buildContractResponse(contract, dataMap, snapshot);
    }
    
    public byte[] loadDocxBytes(Long id, Long currentUserId) throws Exception {
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        permissionChecker.requirePermission(user, "contract.view");
        ContractSnapshot snapshot = contractSnapshotRepository.findByContractId(id)
                .orElseThrow(() -> new IllegalArgumentException("Snapshot không tồn tại"));
        return fileStorage.load(snapshot.getDocxPath()); 
    }
    
    public byte[] loadPdfBytes(Long id, Long currentUserId) throws Exception {
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        permissionChecker.requirePermission(user, "contract.view");
        ContractSnapshot snapshot = contractSnapshotRepository.findByContractId(id)
                .orElseThrow(() -> new IllegalArgumentException("Snapshot không tồn tại"));
        return fileStorage.load(snapshot.getPdfPath());
    }

    public List<ContractListItemResponse> list(Long legalCaseId, Long templateId, Long currentUserId) {
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        permissionChecker.requirePermission(user, "contract.list");
        // Simplified list logic (in production we'd use pagination and criteria builder)
        // Here we just return empty list as placeholder or implement simple find
        return new ArrayList<>();
    }

    private ContractResponse buildContractResponse(Contract contract, java.util.Map<String, String> data, ContractSnapshot snapshot) {
        return ContractResponse.builder()
                .id(contract.getId())
                .contractNo(contract.getContractNo())
                .templateId(contract.getTemplateId())
                .templateVersion(contract.getTemplateVersion())
                .legalCaseId(contract.getLegalCaseId())
                .status(contract.getStatus().name())
                .downloadUrl(ContractDownloadUrlDto.builder()
                        .docx("/api/v1/contracts/" + contract.getId() + "/download/docx")
                        .pdf("/api/v1/contracts/" + contract.getId() + "/download/pdf")
                        .build())
                .renderedHtml(snapshot != null ? snapshot.getRenderedHtml() : null)
                .data(data)
                .createdBy(contract.getCreatedBy())
                .createdAt(contract.getCreatedAt())
                .build();
    }
    

}
