package org.zfin.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.properties.ZfinPropertiesEnum;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for creating file path names and other things.
 */
public final class FileUtil {

    // from http://java.sun.com/j2se/1.5.0/docs/api/java/lang/System.html
    public static final String LINE_SEPARATOR = System.getProperty("line.separator"); // "\n" on unix
    public static final String FILE_SEPARATOR = System.getProperty("file.separator"); // "/" on unix
    public static final String DASH = "-";
    public static final String UNDERSCORE = "_";
    public static final String DOT = ".";
    // folder for data transfer files, including serialized files.
    public static final String DATA_TRANSFER = "data-transfer";

    public static final Logger LOG = LogManager.getLogger(FileUtil.class);
    private static final String WEB_INF = "WEB-INF";

    /**
     * Create an absolute path for a given directory and a file within in.
     * This way to generate the name does not need to know about what path
     * separator is being used. This is done automatically via the File
     * object.
     *
     * @param dir      folder
     * @param fileName file name
     * @return full file path name
     */
    public static String createAbsolutePath(String dir, String fileName) {
        File file = new File(dir);
        File configFile = new File(file, fileName);
        return configFile.getAbsolutePath();
    }

    /**
     * Assumes that the serialized ontology file lives in:
     * webRoot/data-exchange/fileName
     *
     * @param fileName file Name
     * @return boolean
     */
    public static boolean isOntologyFileExist(String fileName) {
        File file = getTomcatDataTransferDirectory();
        File fullFile = new File(file, fileName);
        return fullFile.isFile();
    }

    public static File getFileInTempDirectory(String fileName) {
        String tempDir = System.getProperty("java.io.tmpdir");
        File file = new File(tempDir, fileName);
        return file;
    }

    /**
     * Create an absolute path for a given directory and a file within in.
     * This way to generate the name does not need to know about what path
     * separator is being used. This is done automatically via the File
     * object.
     *
     * @param dir1     first folder
     * @param dir2     second folder
     * @param fileName file name
     * @return absolute path of file.
     */
    public static String createAbsolutePath(String dir1, String dir2, String fileName) {
        File file1 = new File(dir1);
        File file2 = new File(file1, dir2);
        File confFile = new File(file2, fileName);
        return confFile.getAbsolutePath();
    }

    public static File createFile(String dir1, String dir2, String fileName) {
        File file1 = new File(dir1);
        File file2 = new File(file1, dir2);
        return new File(file2, fileName);
    }

    public static File createFileFromDirAndName(String dir, String fileName) {
        File file = new File(dir);
        return new File(file, fileName);
    }

    /**
     * Read content of a file.
     *
     * @param file File
     */
    public static String readFile(File file) {
        String newline = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();
        BufferedReader lnr = null;
        FileReader fw = null;
        try {
            fw = new FileReader(file);
            lnr = new BufferedReader(fw);
            String s;
            while ((s = lnr.readLine()) != null) {
                sb.append(s);
                sb.append(newline);
            }
        } catch (IOException ioe) {
            String message = "Error while reading the file " + file.getAbsolutePath();
            LOG.error(message, ioe);
            throw new RuntimeException(ioe);
        } finally {
            try {
                if (fw != null)
                    fw.close();
                if (lnr != null)
                    lnr.close();
            } catch (IOException e) {
                String message = "Error while closing a stream";
                LOG.error(message, e);
            }
        }
        return sb.toString();
    }

    public static String getCatalinaLogDirectory() {
        return FileUtil.createAbsolutePath(ZfinPropertiesEnum.CATALINA_BASE.value(), "logs");
    }


    /**
     * This method moves (renames) a given file into an archive directory.
     * The file name is changed to include a time stamp.
     *
     * @param file
     * @param archiveFolderPath
     * @param archiveFolderName
     */
    public static File archiveFile(File file, String archiveFolderPath, String archiveFolderName) {
        File dir = new File(archiveFolderPath, archiveFolderName);
        return archiveFile(file, dir);
    }

    /**
     * This method moves (renames) a given file into an archive directory.
     * The file name is changed to include a time stamp:
     * original file name: <file_name.txt>
     * archived file name: <file_name-yyyy-MM-dd_HH_MI_ss.txt>
     * <p/>
     * The archive file is returned.
     * The original file is removed.
     * The archive directory is created if it does not exist.
     *
     * @param file             file that should be archived
     * @param archiveDirectory directory
     * @return File archive file
     */
    public static File archiveFile(File file, File archiveDirectory) {
        // Create the directory if it does not exist yet.
        if (!archiveDirectory.exists()) {
            boolean success = archiveDirectory.mkdirs();
            if (!success)
                LOG.error("Error while creating the archive directory '" + archiveDirectory.getAbsolutePath());
        }

        String fileName = file.getName();

        String archiveFileName = addTimeStampToFileName(fileName);
        File archiveFile = new File(archiveDirectory, archiveFileName);
        if (archiveFile.exists())
            LOG.info("Archive file " + archiveFile.getAbsolutePath() + " already exists. Cannot overwrite it.");
        else {
            boolean success = copyFileIntoArchive(file, archiveFile);
            if (!success)
                LOG.error("Error while renaming the file '" + file.getAbsolutePath());
        }
        return archiveFile;
    }

    public static String addTimeStampToFileName(String fileName) {
        int indexOfLastDot = fileName.lastIndexOf(DOT);
        String extension = fileName.substring(indexOfLastDot + 1);
        StringBuilder archiveFileName = new StringBuilder(fileName.substring(0, indexOfLastDot));
        GregorianCalendar cal = new GregorianCalendar();
        archiveFileName.append(DASH);
        archiveFileName.append(cal.get(Calendar.YEAR));
        archiveFileName.append(cal.get(Calendar.MONTH));
        archiveFileName.append(DASH);
        archiveFileName.append(cal.get(Calendar.DAY_OF_MONTH));
        archiveFileName.append(UNDERSCORE);
        archiveFileName.append(cal.get(Calendar.HOUR));
        archiveFileName.append(UNDERSCORE);
        archiveFileName.append(cal.get(Calendar.MINUTE));
        archiveFileName.append(UNDERSCORE);
        archiveFileName.append(cal.get(Calendar.SECOND));
        archiveFileName.append(DOT);
        archiveFileName.append(extension);
        return archiveFileName.toString();
    }

    public static String addTimeStampToFileName(String fileName, String timestamp) {
        int indexOfLastDot = fileName.lastIndexOf(DOT);
        String extension = fileName.substring(indexOfLastDot + 1);
        StringBuilder archiveFileName = new StringBuilder(fileName.substring(0, indexOfLastDot));
        archiveFileName.append("_");
        archiveFileName.append(timestamp);
        archiveFileName.append(DOT);
        archiveFileName.append(extension);
        return archiveFileName.toString();
    }

    private static boolean copyFileIntoArchive(File file, File archiveFile) {
        boolean success = true;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(file);
            fos = new FileOutputStream(archiveFile);
            FileChannel inChannel = fis.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(48);
            FileChannel outChannel = fos.getChannel();
            while (inChannel.read(buffer) != -1) {
                outChannel.write(buffer);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        } finally {
            try {
                if (fis != null)
                    fis.close();
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
                success = false;
            }
        }
        return success;
    }

    /**
     * Purge an archive directory for files that are older than the purge time.
     *
     * @param archiveDirectory
     * @param purgeTime        in milliseconds
     */
    public static void purgeArchiveDirectory(File archiveDirectory, long purgeTime) {
        File[] files = archiveDirectory.listFiles();
        long time = System.currentTimeMillis();
        for (File file : files) {
            long fileModified = file.lastModified();
            long fileAge = time - fileModified;
            if (fileAge > purgeTime) {
                boolean success = file.delete();
                if (!success)
                    LOG.error("Error while purging archive file '" + file.getAbsolutePath());
            }
        }
    }

    public static File createFileFromStrings(String... paths) {
        File file = null;
        try {
            if (paths != null && paths.length > 0) {
                for (String path : paths) {
                    if (file == null) {
                        file = new File(path);
                    } else {
                        file = new File(file, path);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }
        return file;
    }

    public static boolean checkFileExists(String fileName) {
        File file = new File(fileName);
        if (file.exists())
            return true;
        LOG.error("File not found: " + file.getAbsolutePath());
        return false;
    }

    public static boolean checkFileExists(File file) {
        if (file.exists())
            return true;
        LOG.error("File not found: " + file.getAbsolutePath());
        return false;
    }

    public static File createOntologySerializationFile(String serializedFileName) {
        File file = getTomcatDataTransferDirectory();
        if (!file.exists()) {
            if (!file.mkdir())
                LOG.error("Could not create the directory: " + file.getAbsolutePath());
        }
        return new File(file, serializedFileName);

    }

    public static File getTomcatDataTransferDirectory() {
        String tempDir = System.getProperty("java.io.tmpdir");
        return new File(tempDir, DATA_TRANSFER);
    }

    public static File serializeObject(Object object, File file) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
            outputStream.writeObject(object);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("Error during serialization of file " + file.getAbsolutePath(), e);
        }
        return file;
    }


    public static Object deserializeOntologies(String serializedFileName) throws Exception {
        File file = createOntologySerializationFile(serializedFileName);
        return deserializeOntologies(file);
    }

    public static Object deserializeOntologies(File file) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectInputStream inputStream = new ObjectInputStream(fileInputStream);
//        BufferedReader reader = new BufferedReader(new FileReader(file)) ;
//        xStream.setMode(XStream.ID_REFERENCES) ;
//        ObjectInputStream inputStream = xStream.createObjectInputStream(reader) ;
        return inputStream.readObject();
    }

    /**
     * Create a File from an array of strings that define a path.
     *
     * @param pathElements strings in the order of the file path
     * @return file to the concatenated file or directory
     */
    public static File getFileFromPath(String... pathElements) {
        if (pathElements == null || pathElements.length == 0)
            return null;

        File file = new File(pathElements[0]);
        if (pathElements.length == 1)
            return file;

        for (int index = 1; index < pathElements.length; index++) {
            file = new File(file, pathElements[index]);
        }
        return file;
    }

    public static int countLines(File file) throws IOException {
        LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
        lineNumberReader.skip(Long.MAX_VALUE);
        return lineNumberReader.getLineNumber();
    }

    public static FileInfo getFileInfo(File file) throws IOException {
        FileInfo info = new FileInfo(file);
        info.setNumberOfLines(countLines(file));
        info.setSize(file.length());
        return info;
    }

    /**
     * Strip off slashes from a file path.
     *
     * @param file file name
     * @return remove all prefixes and return pure file name
     */
    public static String getFileNameFromPath(String file) {
        if (StringUtils.isEmpty(file))
            return "";
        int indexOfLastSlash = file.lastIndexOf("/");
        if (indexOfLastSlash < 0)
            return file;
        return file.substring(indexOfLastSlash + 1);
    }

    public static void gunzipFile(String compressedFile) {
        gunzipFile(compressedFile, null);
    }

    public static void gunzipFile(String compressedFile, String decompressedFile) {

        if (decompressedFile == null) {
            decompressedFile = compressedFile.substring(0, compressedFile.indexOf(".gz"));
        }
        byte[] buffer = new byte[1024];

        try {

            FileInputStream fileIn = new FileInputStream(compressedFile);
            GZIPInputStream gZIPInputStream = new GZIPInputStream(fileIn);
            FileOutputStream fileOutputStream = new FileOutputStream(decompressedFile);
            int bytes_read;
            while ((bytes_read = gZIPInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bytes_read);
            }

            gZIPInputStream.close();
            fileOutputStream.close();
            LOG.info("The file " + compressedFile + " was decompressed successfully!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static File gzipFile(String file, boolean removeOriginal) throws IOException {
        try (GZIPOutputStream gzipOS = new GZIPOutputStream(new FileOutputStream(file + ".gz"))) {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    gzipOS.write(buffer, 0, len);
                }
            }
        }
        if (removeOriginal) {
            FileUtils.forceDelete(new File(file));
        }
        return new File(file + ".gz");
    }


    /**
     * Create a zip file with the given content and encoding.
     * @param zipFile The zip file to create. Expect the file to end with ".ORIGINALEXTENSION.zip" (eg. examplefile.html.zip)
     * @param content The content to write to the zip file
     * @param encoding The encoding to use when writing the content
     * @throws IOException
     */
    public static void stringToZipFile(File zipFile, String content, String encoding) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile), StandardCharsets.UTF_8)) {
            zos.setLevel(9);
            // Create a zip entry with the same name as the zip file but without the ".zip" extension
            String entryName = zipFile.getName().replaceFirst("\\.zip$", "");
            zos.putNextEntry(new ZipEntry(entryName));

            // Write the content to the zip entry
            byte[] bytes = content.getBytes(encoding);
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
        }
    }

    /**
     * Writes the specified content to the given file, optionally compressing it into a ZIP format.
     *
     * If the file name ends with ".zip", the content will be written as a zipped file. Otherwise,
     * the content will be written as a plain text file.
     *
     * @param file     the file to write the content to; if the file name ends with ".zip", the content will be zipped
     * @param content  the content to be written to the file
     * @param encoding the encoding to be used when writing the content to the file
     * @throws IOException if an I/O error occurs during writing
     */
    public static void writeToFileOrZip(File file, String content, String encoding) throws IOException {
        if (file.getName().endsWith(".zip")) {
            stringToZipFile(file, content, encoding);
        } else {
            FileUtils.writeStringToFile(file, content, encoding);
        }
    }

    public static void copyURLtoFileIgnoringSSLErrors(String url, File file) throws IOException {
        try {
            // Create a new trust manager that trusts all certificates
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            URL unsafeUrl = new URL(url);
            URLConnection connection = unsafeUrl.openConnection();

            FileUtils.copyURLToFile(connection.getURL(), file);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException("Error while setting up SSL context", e);
        }
    }

    /**
     * Try to create and delete a temporary file in the given directory.
     * If it fails, throw an IOException.
     */
    public static void assertPathWritePermissions(File dirToTest) throws IOException {
        StringBuilder errorOutput = new StringBuilder();

        if (!dirToTest.exists()) {
            throw new IOException("Path does not exist: " + dirToTest);
        }

        if (!dirToTest.isDirectory()) {
            throw new IOException("Path is not a directory: " + dirToTest);
        }

        try {
            File tempFile = File.createTempFile("temporary-file-", ".tmp", dirToTest);
            tempFile.delete();
            return; // Success
        } catch (IOException e) {
            errorOutput.append("Failed to write temporary file to ").append(dirToTest).append("\n");

            try {
                Set<PosixFilePermission> filePerm = Files.getPosixFilePermissions(Paths.get(dirToTest.getAbsolutePath()));
                String permission = PosixFilePermissions.toString(filePerm);
                errorOutput.append("File permissions for ").append(dirToTest).append(": ").append(permission).append("\n");
            } catch (IOException nestedException) {
                errorOutput.append("Could not read file permissions\n");
            }

            try {
                PosixFileAttributes attrs = Files.readAttributes(Paths.get(dirToTest.getAbsolutePath()), PosixFileAttributes.class);
                UserPrincipal owner = attrs.owner();
                GroupPrincipal group = attrs.group();

                int uid = (int) Files.getAttribute(Paths.get(dirToTest.getAbsolutePath()), "unix:uid");
                int gid = (int) Files.getAttribute(Paths.get(dirToTest.getAbsolutePath()), "unix:gid");

                errorOutput.append("Owner: ").append(owner.getName()).append(" (" + uid + ")\n");
                errorOutput.append("Group: ").append(group.getName()).append(" (" + gid + ")\n");
            } catch (IOException nestedException) {
                errorOutput.append("Could not read file ownership information\n");
            }
        }
        if (!errorOutput.isEmpty()) {
            throw new IOException(errorOutput.toString());
        }
    }

}
