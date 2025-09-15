package com.team.OpenGalaxy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;



@Service
public class ChromePdfService {

    /**
     * Finds the path to the Google Chrome or Chromium executable on the current operating system.
     * It checks common installation directories and also allows overriding via an environment variable.
     *
     * @return The absolute path to the Chrome/Chromium executable.
     * @throws IllegalStateException if the executable cannot be found.
     */
    private String findChromeExecutable() {
        // Allow overriding the path with an environment variable for production environments
        String chromePathFromEnv = System.getenv("CHROME_PATH");
        if (chromePathFromEnv != null && new File(chromePathFromEnv).exists()) {
            return chromePathFromEnv;
        }

        String os = System.getProperty("os.name").toLowerCase();
        String[] paths;

        if (os.contains("win")) {
            paths = new String[]{
                    "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                    "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
            };
        } else if (os.contains("mac")) {
            paths = new String[]{
                    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
            };
        } else { // Assume Linux/Unix
            paths = new String[]{
                    "/usr/bin/google-chrome-stable",
                    "/usr/bin/google-chrome",
                    "/usr/bin/chromium-browser",
                    "/usr/bin/chromium"
            };
        }

        for (String path : paths) {
            if (new File(path).exists()) {
                return path;
            }
        }

        throw new IllegalStateException("Google Chrome or Chromium executable not found. " +
                "Please install it or set the CHROME_PATH environment variable.");
    }

    public byte[] renderHtmlToPdfInMemory(String htmlContent) throws IOException, InterruptedException {
        String chromePath = findChromeExecutable();

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
                "--no-sandbox", // Important for running in containerized/server environments
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

