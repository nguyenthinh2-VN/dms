package com.example.be.application.service;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class DocxToHtmlConverter {

    public String convertToHtml(WordprocessingMLPackage wordMLPackage) throws Exception {
        HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
        htmlSettings.setWmlPackage(wordMLPackage);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Docx4J.toHTML(htmlSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);

        return os.toString("UTF-8");
    }
}
