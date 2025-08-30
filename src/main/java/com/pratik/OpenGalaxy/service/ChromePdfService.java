package com.pratik.OpenGalaxy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

//@Service
//public class ChromePdfService {
//
//    @Value("${chrome.path}")
//    private String chromePath; // Set in application.properties
//    public void renderHtmlToPdfUsingChrome(String url, String outputPath) throws IOException, InterruptedException {
//        String chromePath = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
//
//        // Chrome print options as JSON
//        String printOptionsJson = """
//    {
//        "landscape": false,
//        "displayHeaderFooter": false,
//        "printBackground": true,
//        "preferCSSPageSize": true,
//        "paperWidth": 10.42,
//        "paperHeight": 7.81,
//        "marginTop": 0,
//        "marginBottom": 0,
//        "marginLeft": 0,
//        "marginRight": 0
//    }
//    """;
//
//        // Build the Chrome command
//        ProcessBuilder processBuilder = new ProcessBuilder(
//                chromePath,
//                "--headless",
//                "--disable-gpu",
//                "--no-sandbox",
//                "--print-to-pdf=" + outputPath,
//                "--print-to-pdf-no-header",
//                "--virtual-time-budget=10000",
//                url
//        );
//
//        processBuilder.redirectErrorStream(true);
//        Process process = processBuilder.start();
//
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
//            }
//        }
//
//        int exitCode = process.waitFor();
//        if (exitCode != 0) {
//            throw new RuntimeException("Chrome PDF generation failed with exit code " + exitCode);
//        }
//    }

@Service
public class ChromePdfService {

    private final String chromePath = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";

    public byte[] renderHtmlToPdfInMemory(String htmlContent) throws IOException, InterruptedException {
        // Save HTML to temp file
        Path tempHtml = Files.createTempFile("certificate", ".html");
        Files.write(tempHtml, htmlContent.getBytes(StandardCharsets.UTF_8));

        // Save PDF to temp file
        Path tempPdf = Files.createTempFile("certificate", ".pdf");

        // Run Chrome headless to generate PDF
        ProcessBuilder pb = new ProcessBuilder(
                chromePath,
                "--headless",
                "--disable-gpu",
                "--print-to-pdf=" + tempPdf.toAbsolutePath(),
                tempHtml.toUri().toString()
        );
        pb.inheritIO().start().waitFor();

        // Read PDF into byte array
        byte[] pdfBytes = Files.readAllBytes(tempPdf);

        // Clean up temp files
        Files.deleteIfExists(tempHtml);
        Files.deleteIfExists(tempPdf);

        return pdfBytes;
    }
}


