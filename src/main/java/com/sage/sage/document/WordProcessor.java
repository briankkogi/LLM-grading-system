
package com.sage.sage.document;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.FileInputStream;
import java.io.IOException;

public class WordProcessor {

    public String extractTextFromWord(String filePath) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new FileInputStream(filePath))) {
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
            return content.toString();
        }
    }
}
