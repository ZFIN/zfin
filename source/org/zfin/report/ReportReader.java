package org.zfin.report;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Reverse of {@link ReportWriter}: extracts and decodes the embedded {@code window.REPORT_DATA_GZ}
 * payload from an already-rendered report HTML file back into a {@link Report}, so a
 * post-processing step can add to an existing report -- e.g. a new top-level node next to
 * "Added" / "Errors" / "Existing" -- and re-render it, rather than only being able to splice raw
 * HTML around the outside of the viewer.
 *
 * <p>{@link Report} and its nested classes are built with fluent, non-{@code setXxx} methods (see
 * {@link Report}'s own javadoc: "mutable POJOs and fluent setters"), which the default Jackson
 * bean deserializer cannot drive -- it has nothing it recognizes as a setter, and none of these
 * classes declare a constructor Jackson could use instead. The mapper here deserializes by FIELD
 * visibility instead, matching how these classes actually store their state; that requires no
 * change to the model classes themselves, which stay write-oriented (used everywhere else via
 * {@link ReportWriter}) and happen to also be readable this way.
 */
public class ReportReader {

    private static final Pattern DATA_PATTERN =
        Pattern.compile("window\\.REPORT_DATA_GZ\\s*=\\s*\"([^\"]+)\"");

    private final ObjectMapper mapper;

    public ReportReader() {
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /** Reads and decodes the {@code Report} embedded in a previously-written report HTML file. */
    public Report read(Path htmlFile) throws IOException {
        String html = Files.readString(htmlFile, StandardCharsets.UTF_8);
        Matcher m = DATA_PATTERN.matcher(html);
        if (!m.find()) {
            throw new IOException("No window.REPORT_DATA_GZ payload found in " + htmlFile
                + " -- is this a report-template.html-rendered file?");
        }
        byte[] gz = Base64.getDecoder().decode(m.group(1));
        return mapper.readValue(gunzip(gz), Report.class);
    }

    private static byte[] gunzip(byte[] data) throws IOException {
        try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(data));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            return out.toByteArray();
        }
    }
}
