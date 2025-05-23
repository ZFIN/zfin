package org.zfin.datatransfer.ncbi.port;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.text.StringSubstitutor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class PortHelper {

    public static void reportErrAndExit(String msg) {
        String msgWithSubstitutions = StringSubstitutor.replace(msg, System.getenv());
        System.err.println(msgWithSubstitutions);
        System.exit(4);
    }

    public static void sendMailWithAttachedReport(String to, String subject, String attachment) {
        System.out.println("\n========================");
        System.out.println("TODO: handle email");
        String subjectWithSubstitutions = StringSubstitutor.replace(subject, System.getenv());
        System.out.println("EMAIL TO: " + to);
        System.out.println("EMAIL SUBJECT: " + subjectWithSubstitutions);
        System.out.println("EMAIL ATTACHMENT: " + attachment);
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
            out.write(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static long time() {
        long ms = System.currentTimeMillis();
        return ms / 1000;
    }

    public static void assertFileExistsAndNotEmpty(File workingDir, String filename, String errorMessage) {
        File file = new File(workingDir, filename);
        if (!file.exists()) {
            System.err.println(errorMessage);
            System.exit(1);
        }
    }

    public static void rmFile(File workDir, String filename, boolean isWildcard) {
        if (isWildcard) {
            Collection<File> files = FileUtils.listFiles(workDir, new WildcardFileFilter(filename), null);
            files.forEach(FileUtils::deleteQuietly);
        } else {
            FileUtils.deleteQuietly(new File(workDir, filename));
        }
    }

    public static String env(String key) {
        return System.getenv(key);
    }

    public static Boolean envTrue(String key) {
        return envExists(key) && ("true".equals(env(key)) || "1".equals(env(key)));
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
}
