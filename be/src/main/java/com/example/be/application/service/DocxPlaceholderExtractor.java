package com.example.be.application.service;

import com.example.be.application.dto.TemplateFieldDto;
import com.example.be.domain.entity.FieldType;
import org.docx4j.TextUtils;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocxPlaceholderExtractor {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([^}]+)\\s*\\}\\}");
    private static final Pattern VALID_KEY_PATTERN = Pattern.compile("^[a-z_][a-z0-9_]*$");

    public ExtractedData extract(InputStream docxInputStream) throws Exception {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(docxInputStream);
        
        VariablePrepare.prepare(wordMLPackage);
        java.io.StringWriter writer = new java.io.StringWriter();
        TextUtils.extractText(wordMLPackage.getMainDocumentPart().getJaxbElement(), writer);
        String text = writer.toString();
        
        List<String> warnings = new ArrayList<>();
        Set<String> uniqueKeys = new LinkedHashSet<>();
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        while (matcher.find()) {
            String key = matcher.group(1).replaceAll("\\s+", "");
            
            if (!VALID_KEY_PATTERN.matcher(key).matches()) {
                throw new IllegalArgumentException("Biến không hợp lệ: " + key + ". Vui lòng tuân thủ quy ước đặt tên (chỉ dùng chữ thường a-z, số, và dấu gạch dưới).");
            }
            
            if (!hasRecommendedPrefix(key)) {
                warnings.add("Biến '" + key + "' không sử dụng prefix khuyến nghị (party_a_, contract_, ...)");
            }
            
            uniqueKeys.add(key);
        }

        List<TemplateFieldDto> fields = new ArrayList<>();
        int order = 1;
        for (String key : uniqueKeys) {
            TemplateFieldDto field = TemplateFieldDto.builder()
                    .fieldKey(key)
                    .label(humanize(key))
                    .fieldType(guessFieldType(key))
                    .required(true)
                    .displayOrder(order++)
                    .build();
            fields.add(field);
        }

        return new ExtractedData(wordMLPackage, fields, warnings);
    }

    private boolean hasRecommendedPrefix(String key) {
        String[] prefixes = {"party_", "contract_", "payment_", "case_", "signer_"};
        for (String prefix : prefixes) {
            if (key.startsWith(prefix)) return true;
        }
        return false;
    }

    private String humanize(String key) {
        String[] words = key.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    sb.append(word.substring(1));
                }
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    private FieldType guessFieldType(String key) {
        if (key.endsWith("_date")) return FieldType.DATE;
        if (key.endsWith("_value") || key.endsWith("_amount")) return FieldType.MONEY;
        if (key.endsWith("_percent") || key.endsWith("_rate")) return FieldType.NUMBER;
        if (key.endsWith("_text") || key.endsWith("_note") || key.endsWith("_description")) return FieldType.PARAGRAPH;
        return FieldType.TEXT;
    }

    public static class ExtractedData {
        public final WordprocessingMLPackage wordMLPackage;
        public final List<TemplateFieldDto> fields;
        public final List<String> warnings;

        public ExtractedData(WordprocessingMLPackage wordMLPackage, List<TemplateFieldDto> fields, List<String> warnings) {
            this.wordMLPackage = wordMLPackage;
            this.fields = fields;
            this.warnings = warnings;
        }
    }
}
