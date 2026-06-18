package com.example.be.application.usecase;

import com.example.be.application.dto.*;
import com.example.be.application.dto.AnalyzeTemplateResponse;
import com.example.be.application.dto.ContractTemplateResponse;
import com.example.be.application.dto.CreateTemplateRequest;
import com.example.be.application.dto.TemplateFieldDto;
import com.example.be.application.dto.UpdateTemplateRequest;
import com.example.be.application.service.DocxPlaceholderExtractor;
import com.example.be.application.service.DocxToHtmlConverter;
import com.example.be.application.service.PermissionChecker;
import com.example.be.domain.entity.ContractTemplate;
import com.example.be.domain.entity.TemplateField;
import com.example.be.domain.entity.TemplateStatus;
import com.example.be.domain.entity.User;
import com.example.be.domain.repository.ContractFileStorage;
import com.example.be.domain.repository.ContractTemplateRepository;
import com.example.be.domain.repository.TemplateFieldRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContractTemplateUseCase {

    private final ContractTemplateRepository contractTemplateRepository;
    private final TemplateFieldRepository templateFieldRepository;
    private final ContractFileStorage contractFileStorage;
    private final DocxPlaceholderExtractor docxPlaceholderExtractor;
    private final DocxToHtmlConverter docxToHtmlConverter;
    private final PermissionChecker permissionChecker;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContractTemplateUseCase(ContractTemplateRepository contractTemplateRepository,
                                   TemplateFieldRepository templateFieldRepository,
                                   ContractFileStorage contractFileStorage,
                                   DocxPlaceholderExtractor docxPlaceholderExtractor,
                                   DocxToHtmlConverter docxToHtmlConverter,
                                   PermissionChecker permissionChecker) {
        this.contractTemplateRepository = contractTemplateRepository;
        this.templateFieldRepository = templateFieldRepository;
        this.contractFileStorage = contractFileStorage;
        this.docxPlaceholderExtractor = docxPlaceholderExtractor;
        this.docxToHtmlConverter = docxToHtmlConverter;
        this.permissionChecker = permissionChecker;
    }

    public      AnalyzeTemplateResponse analyze(User user, MultipartFile file) throws Exception {
        permissionChecker.requirePermission(user, "contract_template.create");

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }
        
        DocxPlaceholderExtractor.ExtractedData extractedData = docxPlaceholderExtractor.extract(file.getInputStream());
        
        // Normalize placeholders so they are not split across XML tags
        org.docx4j.model.datastorage.migration.VariablePrepare.prepare(extractedData.wordMLPackage);
        String xml = org.docx4j.XmlUtils.marshaltoString(extractedData.wordMLPackage.getMainDocumentPart().getJaxbElement(), true, false);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{(.*?)\\}\\}");
        java.util.regex.Matcher matcher = pattern.matcher(xml);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String matched = matcher.group(0);
            String keyWithoutTags = matched.replaceAll("<[^>]+>", "");
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(keyWithoutTags));
        }
        matcher.appendTail(sb);
        Object obj = org.docx4j.XmlUtils.unmarshalString(sb.toString());
        extractedData.wordMLPackage.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) obj);

        String htmlContent = docxToHtmlConverter.convertToHtml(extractedData.wordMLPackage);

        return AnalyzeTemplateResponse.builder()
                .previewHtml(htmlContent)
                .fields(extractedData.fields)
                .warnings(extractedData.warnings)
                .build();
    }

    @Transactional
    public ContractTemplateResponse save(User user, MultipartFile file, String metadataJson) throws Exception {
        permissionChecker.requirePermission(user, "contract_template.create");

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        CreateTemplateRequest metadata = objectMapper.readValue(metadataJson, CreateTemplateRequest.class);

        DocxPlaceholderExtractor.ExtractedData extractedData = docxPlaceholderExtractor.extract(file.getInputStream());
        
        // Normalize placeholders so they are not split across XML tags
        org.docx4j.model.datastorage.migration.VariablePrepare.prepare(extractedData.wordMLPackage);
        String xml = org.docx4j.XmlUtils.marshaltoString(extractedData.wordMLPackage.getMainDocumentPart().getJaxbElement(), true, false);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{(.*?)\\}\\}");
        java.util.regex.Matcher matcher = pattern.matcher(xml);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String matched = matcher.group(0);
            String keyWithoutTags = matched.replaceAll("<[^>]+>", "");
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(keyWithoutTags));
        }
        matcher.appendTail(sb);
        Object obj = org.docx4j.XmlUtils.unmarshalString(sb.toString());
        extractedData.wordMLPackage.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) obj);

        String htmlContent = docxToHtmlConverter.convertToHtml(extractedData.wordMLPackage);

        int version = 1;
        ContractTemplate existingActive = contractTemplateRepository.findActiveByCode(metadata.getCode()).orElse(null);
        if (existingActive != null) {
            version = existingActive.getVersion() + 1;
            existingActive.setStatus(TemplateStatus.ARCHIVED);
            contractTemplateRepository.save(existingActive);
        }

        String storagePath = "templates/" + metadata.getCode() + "_v" + version + ".docx";
        contractFileStorage.store(file.getBytes(), storagePath);

        ContractTemplate newTemplate = ContractTemplate.builder()
                .code(metadata.getCode())
                .name(metadata.getName())
                .version(version)
                .status(TemplateStatus.ACTIVE)
                .originalFileName(file.getOriginalFilename())
                .storagePath(storagePath)
                .htmlContent(htmlContent)
                .createdBy(user.getId())
                .build();

        ContractTemplate savedTemplate = contractTemplateRepository.save(newTemplate);

        List<TemplateField> fields = metadata.getFields().stream().map(dto -> 
                TemplateField.builder()
                        .templateId(savedTemplate.getId())
                        .fieldKey(dto.getFieldKey())
                        .label(dto.getLabel())
                        .fieldType(dto.getFieldType())
                        .required(dto.isRequired())
                        .displayOrder(dto.getDisplayOrder())
                        .defaultValue(dto.getDefaultValue())
                        .build()
        ).collect(Collectors.toList());

        templateFieldRepository.saveAll(fields);

        return toResponse(savedTemplate, fields);
    }

    public List<ContractTemplateResponse> listActive(User user) {
        permissionChecker.requirePermission(user, "contract_template.list");

        List<ContractTemplate> templates = contractTemplateRepository.findByStatus(TemplateStatus.ACTIVE);
        return templates.stream().map(t -> {
            ContractTemplateResponse res = toResponse(t, null);
            res.setHtmlContent(null);
            return res;
        }).collect(Collectors.toList());
    }

    public ContractTemplateResponse getById(User user, Long id) {
        permissionChecker.requirePermission(user, "contract_template.view");

        ContractTemplate template = contractTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mẫu hợp đồng: " + id));

        List<TemplateField> fields = templateFieldRepository.findByTemplateId(id);
        return toResponse(template, fields);
    }

    @Transactional
    public ContractTemplateResponse archive(User user, Long id) {
        permissionChecker.requirePermission(user, "contract_template.archive");

        ContractTemplate template = contractTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mẫu hợp đồng: " + id));

        template.setStatus(TemplateStatus.ARCHIVED);
        ContractTemplate saved = contractTemplateRepository.save(template);
        
        List<TemplateField> fields = templateFieldRepository.findByTemplateId(id);
        return toResponse(saved, fields);
    }

    @Transactional
    public ContractTemplateResponse update(User user, Long id, MultipartFile file, String metadataJson) throws Exception {
        permissionChecker.requirePermission(user, "contract_template.update");

        ContractTemplate template = contractTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mẫu hợp đồng: " + id));

        UpdateTemplateRequest metadata = null;
        if (metadataJson != null && !metadataJson.isEmpty()) {
            metadata = objectMapper.readValue(metadataJson, UpdateTemplateRequest.class);
        }

        if (file != null && !file.isEmpty()) {
            template.setStatus(TemplateStatus.ARCHIVED);
            contractTemplateRepository.save(template);

            int newVersion = template.getVersion() + 1;
            DocxPlaceholderExtractor.ExtractedData extractedData = docxPlaceholderExtractor.extract(file.getInputStream());
            
            // Normalize placeholders so they are not split across XML tags
            org.docx4j.model.datastorage.migration.VariablePrepare.prepare(extractedData.wordMLPackage);
            String xml = org.docx4j.XmlUtils.marshaltoString(extractedData.wordMLPackage.getMainDocumentPart().getJaxbElement(), true, false);
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{(.*?)\\}\\}");
            java.util.regex.Matcher matcher = pattern.matcher(xml);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String matched = matcher.group(0);
                String keyWithoutTags = matched.replaceAll("<[^>]+>", "");
                matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(keyWithoutTags));
            }
            matcher.appendTail(sb);
            Object obj = org.docx4j.XmlUtils.unmarshalString(sb.toString());
            extractedData.wordMLPackage.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) obj);

            String htmlContent = docxToHtmlConverter.convertToHtml(extractedData.wordMLPackage);

            String storagePath = "templates/" + template.getCode() + "_v" + newVersion + ".docx";
            contractFileStorage.store(file.getBytes(), storagePath);

            ContractTemplate newTemplate = ContractTemplate.builder()
                    .code(template.getCode())
                    .name(metadata != null ? metadata.getName() : template.getName())
                    .version(newVersion)
                    .status(TemplateStatus.ACTIVE)
                    .originalFileName(file.getOriginalFilename())
                    .storagePath(storagePath)
                    .htmlContent(htmlContent)
                    .createdBy(user.getId())
                    .build();

            ContractTemplate savedTemplate = contractTemplateRepository.save(newTemplate);

            List<TemplateField> newFields;
            if (metadata != null && metadata.getFields() != null && !metadata.getFields().isEmpty()) {
                newFields = metadata.getFields().stream().map(dto ->
                        TemplateField.builder()
                                .templateId(savedTemplate.getId())
                                .fieldKey(dto.getFieldKey())
                                .label(dto.getLabel())
                                .fieldType(dto.getFieldType())
                                .required(dto.isRequired())
                                .displayOrder(dto.getDisplayOrder())
                                .defaultValue(dto.getDefaultValue())
                                .build()
                ).collect(Collectors.toList());
            } else {
                List<TemplateField> oldFields = templateFieldRepository.findByTemplateId(id);
                newFields = oldFields.stream().map(f -> {
                    TemplateField copy = TemplateField.builder()
                            .templateId(savedTemplate.getId())
                            .fieldKey(f.getFieldKey())
                            .label(f.getLabel())
                            .fieldType(f.getFieldType())
                            .required(f.isRequired())
                            .displayOrder(f.getDisplayOrder())
                            .defaultValue(f.getDefaultValue())
                            .build();
                    return copy;
                }).collect(Collectors.toList());
            }

            templateFieldRepository.saveAll(newFields);
            return toResponse(savedTemplate, newFields);
        } else {
            if (metadata != null) {
                template.setName(metadata.getName());
                ContractTemplate savedTemplate = contractTemplateRepository.save(template);
                
                if (metadata.getFields() != null && !metadata.getFields().isEmpty()) {
                    templateFieldRepository.deleteByTemplateId(template.getId());
                    
                    List<TemplateField> newFields = metadata.getFields().stream().map(dto ->
                            TemplateField.builder()
                                    .templateId(savedTemplate.getId())
                                    .fieldKey(dto.getFieldKey())
                                    .label(dto.getLabel())
                                    .fieldType(dto.getFieldType())
                                    .required(dto.isRequired())
                                    .displayOrder(dto.getDisplayOrder())
                                    .defaultValue(dto.getDefaultValue())
                                    .build()
                    ).collect(Collectors.toList());
                    templateFieldRepository.saveAll(newFields);
                    return toResponse(savedTemplate, newFields);
                }
                
                return toResponse(savedTemplate, templateFieldRepository.findByTemplateId(template.getId()));
            }
            
            return toResponse(template, templateFieldRepository.findByTemplateId(template.getId()));
        }
    }

    private ContractTemplateResponse toResponse(ContractTemplate template, List<TemplateField> fields) {
        List<TemplateFieldDto> fieldDtos = null;
        if (fields != null) {
            fieldDtos = fields.stream().map(f -> TemplateFieldDto.builder()
                    .fieldKey(f.getFieldKey())
                    .label(f.getLabel())
                    .fieldType(f.getFieldType())
                    .required(f.isRequired())
                    .displayOrder(f.getDisplayOrder())
                    .defaultValue(f.getDefaultValue())
                    .build()).collect(Collectors.toList());
        }

        return ContractTemplateResponse.builder()
                .id(template.getId())
                .code(template.getCode())
                .name(template.getName())
                .version(template.getVersion())
                .status(template.getStatus())
                .originalFileName(template.getOriginalFileName())
                .htmlContent(template.getHtmlContent())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .fields(fieldDtos)
                .build();
    }
}
