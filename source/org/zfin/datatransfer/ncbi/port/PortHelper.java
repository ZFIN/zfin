package org.zfin.datatransfer.ncbi.port;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.StandardCopyOption;
import org.apache.commons.codec.binary.Hex;

import static org.zfin.util.DateUtil.nowToString;


public class PortHelper {

    public static void reportErrAndExit(String msg) {
        // Ensure environment variables are substituted if the pattern ${VAR_NAME} is used
        Map<String, String> envMap = System.getenv();
        StringSubstitutor sub = new StringSubstitutor(envMap);
        String resolvedMsg = sub.replace(msg);
        System.err.println(resolvedMsg);
        // Consider closing resources or other cleanup before exiting
        System.exit(4);
    }


    public static void sendMailWithAttachedReport(String to, String subject, String attachmentFilename, File workingDir) {
        sendMailWithAttachedReport(to, subject, new File(workingDir, attachmentFilename).getAbsolutePath());
    }

    public static void sendMailWithAttachedReport(String to, String subject, String attachmentFilename) {
        System.out.println("\n========================");
        System.out.println("TODO: handle email");
        Map<String, String> envMap = System.getenv();
        StringSubstitutor sub = new StringSubstitutor(envMap);
        String resolvedSubject = sub.replace(subject);

        System.out.println("EMAIL TO: " + to);
        System.out.println("EMAIL SUBJECT: " + resolvedSubject);
        System.out.println("EMAIL ATTACHMENT: " + attachmentFilename);

        String copyOfAttachmentFilename = new File(attachmentFilename).getName() + nowToString("--yyyy-MM-dd-HH-mm-ss");
        try {
            FileUtils.copyFile(new File(attachmentFilename), new File("/tmp/" + copyOfAttachmentFilename));
        } catch (IOException e) {
            System.err.println("Could not copy attachment file: " + attachmentFilename);
        }
        System.out.println("ATTACHMENT: /tmp/" + copyOfAttachmentFilename);
        // Actual email sending logic would go here.
        // For now, just printing.
        System.out.println("========================\n");
    }

    public static void unless(boolean b, Runnable o) {
        if (!b) {
            o.run();
        }
    }

    public static void print(String s) {
        System.out.println(s);
    }

    public static void print(BufferedWriter out, String s) {
        try {
            if (out != null) {
                out.write(s);
                // out.newLine(); // Consider if new lines are automatically handled or needed
            } else {
                System.err.println("Attempted to write to a null BufferedWriter.");
            }
        } catch (IOException e) {
            // Consider a more robust error handling strategy
            throw new RuntimeException("Failed to write to BufferedWriter", e);
        }
    }

    public static long time() {
        long ms = System.currentTimeMillis();
        return ms / 1000; // Returns time in seconds
    }

    public static void assertFileExistsAndNotEmpty(File workingDir, String filename, String errorMessage) {
        File file = new File(workingDir, filename);
        if (!file.exists()) {
            System.err.println(errorMessage + " (File does not exist: " + file.getAbsolutePath() + ")");
            System.exit(1);
        }
        if (file.length() == 0) {
            System.err.println(errorMessage + " (File is empty: " + file.getAbsolutePath() + ")");
            // Original Perl script does not exit here for emptiness, but it's a good check.
            // System.exit(1); // Or handle as a warning
        }
    }

    public static void rmFiles(File workDir, List<String> filenamePatterns) {
        for (String filename : filenamePatterns) {
            rmFile(workDir, filename);
        }
    }
    public static void rmFile(File workDir, String filenamePattern) {
        if (filenamePattern.contains("*")) {
            rmFile(workDir, filenamePattern, true);
        } else {
            rmFile(workDir, filenamePattern, false);
        }
    }
    public static void rmFile(File workDir, String filenamePattern, boolean isWildcard) {
        if (isWildcard) {
            try {
                Collection<File> files = FileUtils.listFiles(workDir, new WildcardFileFilter(filenamePattern), null);
                for (File file : files) {
                    try {
                        Files.deleteIfExists(file.toPath());
                    } catch (IOException e) {
                        System.err.println("Failed to delete file: " + file.getAbsolutePath() + " - " + e.getMessage());
                    }
                }
            } catch (Exception e) { // Catch broader exceptions from FileUtils if any occur
                System.err.println("Error listing files for deletion with pattern " + filenamePattern + " in " + workDir.getAbsolutePath() + ": " + e.getMessage());
            }
        } else {
            Path path = Paths.get(workDir.getAbsolutePath(), filenamePattern);
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                System.err.println("Failed to delete file: " + path.toString() + " - " + e.getMessage());
            }
        }
    }


    public static String env(String key) {
        return System.getenv(key);
    }

    public static Boolean envTrue(String key) {
        String value = env(key);
        return value != null && ("true".equalsIgnoreCase(value) || "1".equals(value));
    }

    public static void assertEnvironment(String ...envVars) {
        boolean success = true;
        for (String envVar : envVars) {
            if (!envExists(envVar)) {
                System.out.println("Missing environment variable " + envVar);
                success = false;
            }
        }
        if (!success) {
            System.exit(2);
        }
    }

    public static boolean envExists(String key) {
        return System.getenv(key) != null;
    }

    public static boolean stringStartsWithLetter(String s) {
        return s != null && !s.isEmpty() && Character.isLetter(s.charAt(0));
    }

    public static String getArtifactComparisonURLs() {
        String jobURL = env("JOB_URL");
        String buildDisplayName = env("BUILD_DISPLAY_NAME");

        if (StringUtils.isBlank(jobURL) || StringUtils.isBlank(buildDisplayName)) {
            return "";
        }

        // BUILD_DISPLAY_NAME is usually like "#1234"
        String buildIDStr = buildDisplayName.replace("#", "");
        if (!buildIDStr.matches("\\d+")) {
            return ""; // Not a numeric build ID
        }

        int buildID;
        try {
            buildID = Integer.parseInt(buildIDStr);
        } catch (NumberFormatException e) {
            return ""; // Should not happen due to regex check, but good practice
        }

        // Ensure jobURL ends with a slash
        if (!jobURL.endsWith("/")) {
            jobURL += "/";
        }

        // Replace http with https if necessary
        if (jobURL.startsWith("http:")) {
            jobURL = "https" + jobURL.substring(4);
        }

        int previousBuildID = buildID - 1;
        if (previousBuildID <= 0) {
            return ""; // No previous build to compare against or invalid build ID
        }

        String[] artifacts = {
                "reportNtoN", "reportNtoOne", "reportOneToN", "reportOneToZero",
                "reportStatistics", "reportNonLoadPubGenPept", "reportZDBgeneIdWithMultipleVegaIds"
        };

        StringBuilder buffer = new StringBuilder("See below URLs to compare reports between this build and the previous build:\n");
        buffer.append("===========================================================================\n\n");
        for (String artifact : artifacts) {
            // Example URL: https://SITE.zfin.org/job/JOB_NAME/PREVIOUS_BUILD_ID/artifact-diff/CURRENT_BUILD_ID/path/to/artifact
            buffer.append(String.format("%s%d/artifact-diff/%d/%s\n",
                    jobURL, previousBuildID, buildID, artifact));
        }
        buffer.append("===========================================================================\n\n");

        return buffer.toString();
    }

    public static void downloadOrUseLocalFile(String urlString, File localFile, File workingDir, BufferedWriter log) throws IOException {
        if (envTrue("SKIP_DOWNLOADS")) {
            print(log, "SKIP_DOWNLOADS is true. Using local file: " + localFile.getAbsolutePath() + "\n");
            if (!localFile.exists()) {
                reportErrAndExit("SKIP_DOWNLOADS is true, but local file not found: " + localFile.getAbsolutePath());
            }
            return;
        }

        print(log, "Downloading " + urlString + " to " + localFile.getAbsolutePath() + "\n");
        URL url = new URL(urlString);
        try (InputStream in = url.openStream()) {
            Files.copy(in, localFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static String md5File(File file, BufferedWriter log) {
        if (!file.exists()) {
            print(log, "File not found for MD5 calculation: " + file.getAbsolutePath() + "\n");
            return null; // Or throw an exception
        }
        try (InputStream fis = Files.newInputStream(file.toPath())) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int count;
            while ((count = fis.read(buffer)) > 0) {
                md.update(buffer, 0, count);
            }
            byte[] digest = md.digest();
            return Hex.encodeHexString(digest);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}