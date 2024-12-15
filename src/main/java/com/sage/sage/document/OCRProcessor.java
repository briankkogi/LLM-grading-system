package com.sage.sage.document;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class OCRProcessor {

    public String extractTextFromImage(String imagePath) throws TesseractException, IOException {
        // Preprocess the image
        String preprocessedImagePath = preprocessImage(imagePath);

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("/opt/homebrew/Cellar/tesseract/5.5.0/share/tessdata"); // Update if necessary
        tesseract.setLanguage("eng"); // Set language to English
        tesseract.setPageSegMode(6); // Set segmentation mode
        tesseract.setTessVariable("tessedit_char_whitelist", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,!? ");

        try {
            return tesseract.doOCR(new File(preprocessedImagePath));
        } finally {
            // Clean up the temporary file
            new File(preprocessedImagePath).delete();
        }
    }

    private String preprocessImage(String imagePath) throws IOException {
        // Load the image
        BufferedImage inputImage = ImageIO.read(new File(imagePath));

        // Convert to grayscale
        BufferedImage grayscaleImage = new BufferedImage(
                inputImage.getWidth(),
                inputImage.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );
        Graphics2D g2d = grayscaleImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, null);
        g2d.dispose();

        // Apply binary thresholding
        BufferedImage binaryImage = new BufferedImage(
                grayscaleImage.getWidth(),
                grayscaleImage.getHeight(),
                BufferedImage.TYPE_BYTE_BINARY
        );
        Graphics2D g2dBinary = binaryImage.createGraphics();
        g2dBinary.drawImage(grayscaleImage, 0, 0, null);
        g2dBinary.dispose();

        // Save the preprocessed image to a temporary file
        File tempFile = Files.createTempFile("preprocessed_image", ".png").toFile();
        ImageIO.write(binaryImage, "png", tempFile);

        return tempFile.getAbsolutePath();
    }
}