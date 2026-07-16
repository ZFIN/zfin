package org.zfin.report;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

/**
 * Serializes a {@link Report} to JSON and inlines it into the report HTML
 * template by replacing the section between the {@code //PLACEHOLDER_START}
 * and {@code //PLACEHOLDER_END} markers.
 *
 * <p>The default template is loaded from the classpath as a sibling resource
 * of this class ({@code report-template.html}). Tests and one-off callers may
 * pass an alternative template via the {@code File}-taking overloads.
 */
public class ReportWriter {

    public static final String TEMPLATE_RESOURCE = "report-template.html";

    private static final String PLACEHOLDER_START = "//PLACEHOLDER_START";
    private static final String PLACEHOLDER_END   = "//PLACEHOLDER_END";

    // Compact (non-indented) JSON: the payload is machine-read by the report
    // viewer, never hand-edited, so indentation is pure byte bloat in a file
    // that can grow large.
    private final ObjectMapper mapper = new ObjectMapper();

    /** Render using the bundled classpath template. */
    public String render(Report report) throws IOException {
        return inlineInto(report, loadDefaultTemplate());
    }

    public String render(Report report, File templateFile) throws IOException {
        return inlineInto(report, Files.readString(templateFile.toPath(), StandardCharsets.UTF_8));
    }

    public void write(Report report, File outputHtml) throws IOException {
        Files.writeString(outputHtml.toPath(), render(report), StandardCharsets.UTF_8);
    }

    public void write(Report report, File outputHtml, File templateFile) throws IOException {
        Files.writeString(outputHtml.toPath(), render(report, templateFile), StandardCharsets.UTF_8);
    }

    private String inlineInto(Report report, String template) throws IOException {
        int start = template.indexOf(PLACEHOLDER_START);
        int end   = start < 0 ? -1 : template.indexOf(PLACEHOLDER_END, start);
        if (start < 0 || end < 0) {
            throw new IOException("Template missing " + PLACEHOLDER_START + " / "
                + PLACEHOLDER_END + " markers (or end precedes start).");
        }
        // Gzip then Base64-encode the JSON: report payloads are highly
        // repetitive and compress ~5-15x, and the viewer inflates them with the
        // browser's native DecompressionStream. Base64 is also <script>-safe by
        // construction — its alphabet can't contain "</" to break out of the tag
        // or reproduce our PLACEHOLDER_END sentinel — so no escaping is needed.
        String b64 = gzipBase64(mapper.writeValueAsBytes(report));
        String payload = PLACEHOLDER_START
            + "\n        window.REPORT_DATA_GZ = \"" + b64 + "\";\n        ";
        return template.substring(0, start) + payload + template.substring(end);
    }

    private static String gzipBase64(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(bos)) {
            gz.write(data);
        }
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    private String loadDefaultTemplate() throws IOException {
        try (InputStream in = ReportWriter.class.getResourceAsStream(TEMPLATE_RESOURCE)) {
            if (in == null) {
                throw new IOException("Could not find classpath resource " + TEMPLATE_RESOURCE
                    + " next to " + ReportWriter.class.getName());
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
