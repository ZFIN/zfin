package org.zfin.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.zfin.framework.presentation.ZfinFilenameFilter;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

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

    public static final Logger LOG = Logger.getLogger(FileUtil.class);
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
        String tempDir = System.getProperty("java.io.tmpdir");
        File file = new File(tempDir, DATA_TRANSFER);
        File fullFile = new File(file, fileName);
        return fullFile.isFile();
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
     * @param file
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
     * @param archiveDirectory
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

    public static List<File> countApgFiles() {
        File dir = new File(ZfinPropertiesEnum.WEBROOT_DIRECTORY.value(), "ZFIN");
        File appDir = new File(dir, "APP_PAGES");
        FilenameFilter filter = new ZfinFilenameFilter(".apg");
        return getAllFiles(appDir, filter);
    }

    public static List<File> countJspFiles() {
        File dir = new File(ZfinPropertiesEnum.WEBROOT_DIRECTORY.value());
        FilenameFilter filter = new ZfinFilenameFilter(".jsp");
        return getAllFiles(dir, filter);
    }

    public static List<File> countClassFiles() {
        File web = new File(ZfinPropertiesEnum.WEBROOT_DIRECTORY.value());
        File webInf = new File(web, WEB_INF);
        File classesDir = new File(webInf, "classes");
        File org = new File(classesDir, "org");
        File zfin = new File(org, "zfin");
        FilenameFilter filter = new ZfinFilenameFilter(".class");
        return getAllFiles(zfin, filter);
    }

    public static List<File> getAllFiles(File dir, FilenameFilter filter) {
        if (dir == null)
            return null;

        List<File> list = new ArrayList<File>();
        File[] files = dir.listFiles(filter);
        if (files == null)
            return null;

        for (File file : files) {
            if (file.isDirectory()) {
                List<File> filesFromSubDir = getAllFiles(file, filter);
                if (filesFromSubDir != null)
                    list.addAll(filesFromSubDir);
            } else {
                list.add(file);
            }

        }
        return list;
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
        }
        catch (Exception e) {
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

    public static File createOntologySerializationFile(String serializedFileName) {
        String tempDir = System.getProperty("java.io.tmpdir");
        File file = new File(tempDir, DATA_TRANSFER);
        if (!file.exists()) {
            if (!file.mkdir())
                LOG.error("Could not create the directory: " + file.getAbsolutePath());
        }
        return new File(file, serializedFileName);

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
        return inputStream.readObject();
    }

    /**
     * TODO: Convert to use IOUtils.readLines().size(); ?
     * TODO: remove?  not used be any active methods as far as I can tell
     * @param filename
     * @return
     * @throws IOException
     */
    public static int countLines(String filename) throws IOException {
        LineNumberReader reader = new LineNumberReader(new FileReader(filename));
        int cnt;
        String lineRead = "";
        while ((lineRead = reader.readLine()) != null) {
        }
        cnt = reader.getLineNumber();
        reader.close();
        return cnt;
    }

}
