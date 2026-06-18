package com.example.be.application.service;

import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Text;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class DocxMergeService {

    public byte[] merge(byte[] templateDocx, Map<String, String> data) throws Exception {
        try (ByteArrayInputStream is = new ByteArrayInputStream(templateDocx);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(is);
            
            // Prepare variable to merge run texts properly (works for ${} usually)
            VariablePrepare.prepare(wordMLPackage);

            // Convert main document to XML string to handle {{ }} variables that got split by Word formatting tags
            String xml = org.docx4j.XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, false);

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{(.*?)\\}\\}");
            java.util.regex.Matcher matcher = pattern.matcher(xml);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String matched = matcher.group(0);
                // Clean up any internal XML tags to get the pure variable name
                String keyWithoutTags = matched.replaceAll("<[^>]+>", "");
                String variableName = keyWithoutTags.substring(2, keyWithoutTags.length() - 2).trim();

                if (data.containsKey(variableName)) {
                    String safeValue = escapeXml(data.get(variableName));
                    matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(safeValue));
                } else {
                    matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(matched));
                }
            }
            matcher.appendTail(sb);

            Object obj = org.docx4j.XmlUtils.unmarshalString(sb.toString());
            wordMLPackage.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) obj);

            wordMLPackage.save(os);
            return os.toByteArray();
        }
    }

    private String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }
}
