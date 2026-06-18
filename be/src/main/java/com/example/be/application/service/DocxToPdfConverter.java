package com.example.be.application.service;

import lombok.extern.slf4j.Slf4j;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Slf4j
@Service
public class DocxToPdfConverter {

    public byte[] convertToPdf(byte[] docxBytes) throws Exception {
        try (ByteArrayInputStream is = new ByteArrayInputStream(docxBytes);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            // Load Word document using Apache POI
            XWPFDocument document = new XWPFDocument(is);

            // Setup PDF options
            PdfOptions options = PdfOptions.create();

            // Export to PDF using XDocReport
            PdfConverter.getInstance().convert(document, os, options);

            return os.toByteArray();
        }
    }
}
