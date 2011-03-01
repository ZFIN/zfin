package org.zfin.datatransfer.microarray;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import org.zfin.datatransfer.service.DownloadService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.GZIPInputStream;


public abstract class AbstractGeoSoftProcessor implements SoftParser {

    private Logger logger = Logger.getLogger(AbstractGeoSoftProcessor.class);

    protected DownloadService downloadService;

    // parsing stuff stuff
    private int accessionColumn = 2;
    private String[] includePatterns = null;
    private String[] excludePatterns = null;
    private String[] defaultAccessionExcludePatterns = new String[]{"XM_", "XR_", "ENSDART", "ETG", "NC_", "NG_", "DRPTA",
            "A_", "DCP_", "NP", "NR_", "TC"};
    private final static String DOT = ".";
    private final static String UNDERSCORE = "_";

    // download stuff
    private String fileName;
    protected String urlString = "ftp.ncbi.nih.gov";
    protected String tempDirectory = System.getProperty("java.io.tmpdir", null);
    protected String workingDirectory;
    protected String fileNameSuffix = "_family.soft";
    protected String gzipSuffix = ".gz";
    protected String userName = "anonymous";
    protected String password = "zfinadmn@zfin.org";
    private int FTP_TIMEOUT = 3000; // ftp timeout in milliseconds
    private boolean alwaysUseExistingFile = false;
    private final int BUFFER_SIZE = 1024;

    public Set<String> parseUniqueNumbers(String fileName, int column) {
        return parseUniqueNumbers(fileName, column, null, null);
    }


    public Set<String> parseUniqueNumbers(String fileName, int column, String[] includePattern) {
        return parseUniqueNumbers(fileName, column, includePattern, null);
    }


    public Set<String> parseUniqueNumbers(String fileName, int column, String[] includePattern, String[] excludePattern) {
        setAccessionColumn(column);
        setFileName(fileName);
        setIncludePatterns(includePattern);
        setExcludePatterns(excludePattern);
//        File file = downloadFile() ;
        File file = null;
        try {
            file = downloadService.downloadGzipFile(
                    new File(System.getProperty("java.io.tmpdir") + "/" + getFileName() + fileNameSuffix)
                    , new URL("ftp://" + urlString + "/pub/geo/DATA/SOFT/by_platform/" + this.fileName + "/" + getFileName() + fileNameSuffix + gzipSuffix)
                    , false);
        } catch (MalformedURLException e) {
            logger.error("bad URL", e);
            return new HashSet<String>();
        }
        return parseUniqueNumbers(file);
    }

    @Override
    public void setAlwaysUseExistingFile(boolean alwaysUseExistingFile) {
        this.alwaysUseExistingFile = alwaysUseExistingFile;
    }

    @Override
    public boolean isAlwaysUseExistingFile() {
        return this.alwaysUseExistingFile;
    }

    /**
     * Checks to see if there is a newer file and downloads and decompresses.
     *
     * @return The location of the local file.
     */
    public File downloadFile() {
        File localFile = new File(tempDirectory + "/" + getFileName() + fileNameSuffix);
        String remoteFileName = getFileName() + fileNameSuffix + gzipSuffix;
        try {
            workingDirectory = "pub/geo/DATA/SOFT/by_platform/" + this.fileName;
            FTPClient ftpClient = new FTPClient();
            ftpClient.setDataTimeout(FTP_TIMEOUT);
            ftpClient.connect(urlString);
            ftpClient.setBufferSize(BUFFER_SIZE);
            ftpClient.login(userName, password);

            ftpClient.changeWorkingDirectory(workingDirectory);

            ftpClient.enterLocalPassiveMode();
            FTPFile[] ftpFiles = ftpClient.listFiles(getFileName() + fileNameSuffix + gzipSuffix);

            if (ftpFiles.length != 1) {
                String errorText = "Problem retrieving file from NCBI of name: " +
                        getFileName() + fileNameSuffix + gzipSuffix;
                logger.error(errorText);
                throw new RuntimeException(errorText);
            }

            FTPFile ftpFile = ftpFiles[0];

            long remoteFileSize = ftpFile.getSize(); // this file is gzipped
            long localFileSize = localFile.length(); // ths file is not

            // do we have a local file?
            logger.info("downloaded file should be here: " + localFile.getAbsolutePath());
            if (localFile.exists()) {
                logger.info("local file date: " + new Date(localFile.lastModified()));
                logger.info("local expanded file size: " +
                        NumberFormat.getInstance().format(((double) localFileSize / (1024 * 1024))) + " Mb");
                logger.info("remote gzipped file size: " +
                        NumberFormat.getInstance().format(((double) remoteFileSize / (1024 * 1024))) + " Mb");

            } else {
                logger.info("no local file available");
            }


            logger.info("remote file date: " + ftpFile.getTimestamp().getTime());
            // if the local file does not exist
            // if the local file timestamp is older than the new one
            if (
                    (localFile.exists() && localFile.lastModified() < ftpFile.getTimestamp().getTimeInMillis()
                            && false == alwaysUseExistingFile)
                            ||
                            (false == localFile.exists())
                            ||
                            (localFileSize < remoteFileSize)
                    ) {
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                localFile.createNewFile();
                logger.info("starting to download to: " + localFile + " size: " +
                        NumberFormat.getInstance().format((ftpFile.getSize() / (1024 * 1024))) + " Mb");
                FileOutputStream fileOutputStream = new FileOutputStream(localFile);
                ftpClient.enterLocalPassiveMode();
                long startTime = System.currentTimeMillis();
                GZIPInputStream gzipInputStream =
                        new GZIPInputStream(ftpClient.retrieveFileStream(remoteFileName));
                byte[] buf = new byte[BUFFER_SIZE];
                int len;
                while ((len = gzipInputStream.read(buf)) > 0) {
                    fileOutputStream.write(buf, 0, len);
                }
                long endTime = System.currentTimeMillis();
                int downloadTime = (int) (endTime - startTime) / 1000;
                double downloadRate = (double) remoteFileSize / (1024 * 1024) / downloadTime;
                logger.info("finished download to: " + localFile);
                logger.info("Time: " + downloadTime + "(s) Rate: " + downloadRate + " Mb/s");

                gzipInputStream.close();
                fileOutputStream.close();
                ftpClient.logout();
                localFile.setLastModified(ftpFile.getTimestamp().getTimeInMillis());
                return localFile;
            } else {
                logger.info("newer file not detected and files are of appropriate size: " + getFileName() + fileNameSuffix);
            }

            ftpClient.logout();
            return localFile;
        } catch (SocketException socketException) {
            logger.error("Failed to connect for file, will try to use existing file: " + fileName, socketException.fillInStackTrace());
            if (localFile.exists()) {
                logger.warn("File not updated, using existing file: " + fileName);
                return localFile;
            } else {
                logger.error("No file exists, so nothing to update" + fileName);
                throw new RuntimeException("stop parsing, bad IO connection and file not " +
                        "downloaded and no file exists", socketException);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file properly, bailing out of load for file: " + getFileName(), e);
        }
    }


    public Set<String> parseUniqueNumbers(File file) {

        Set<String> accessionNumbers = new HashSet<String>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String buffer;

            // read from input file to output file
            boolean startReading = false;
            int count = 0;
            while ((buffer = reader.readLine()) != null) {
                if (buffer.startsWith("!platform_table_end")) {
                    logger.info("number of geo accession from " + getFileName() + ": " + count);

                    // read first 3 here
                    Iterator<String> iter = accessionNumbers.iterator();
                    for (int i = 0; iter.hasNext(); i++) {
//                        for(int i = 0 ; iter.hasNext() && i < 3 ; i++){
                        logger.debug(getFileName() + " col: " + getAccessionColumn() + ": " + iter.next());
                    }
                    return accessionNumbers;
                } else if (startReading == true && doInclude(buffer) && doExclude(buffer)) {
                    String accessionNumber = parseLine(buffer);
                    if (isValidAccessionNumber(accessionNumber)) {
                        accessionNumbers.add(accessionNumber);
                        ++count;
                    }
                } else if (buffer.startsWith("!platform_table_begin")) {
                    startReading = true;
                    // skip header, which they all have
                    reader.readLine();
                }
            }

        } catch (Exception e) {
            logger.error("fail to parse: " + e.fillInStackTrace());
        } finally {
            if (file != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    logger.error("fail to close: " + e.fillInStackTrace());
                }
            }
        }
        return accessionNumbers;


    }

    private boolean isValidAccessionNumber(String accessionNumber) {
        if (accessionNumber == null) return false;

        if (defaultAccessionExcludePatterns != null) {
            for (String excludePattern : defaultAccessionExcludePatterns) {
                if (accessionNumber.contains(excludePattern)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param buffer String to check.
     * @return True if buffer  does not include either an excluded or defaultExcluded pattern.
     */
    private boolean doExclude(String buffer) {
        if (excludePatterns != null) {
            for (String excludePattern : excludePatterns) {
                if (buffer.contains(excludePattern)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param buffer String to check.
     * @return True if buffer contains the included pattern.
     */
    private boolean doInclude(String buffer) {
        if (includePatterns == null || includePatterns.length == 0) {
            return true;
        }
        for (String includePattern : includePatterns) {
            if (false == buffer.contains(includePattern)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Destructive method.
     *
     * @param accession Accession to fix.
     * @return Accession without a . or _ in the wrong spot.
     */
    public String fixAccession(String accession) {
        accession = cleanDot(accession);
        accession = cleanUnderscore(accession);
        return accession;
    }

    private String cleanUnderscore(String accession) {
        int dotIndex = accession.lastIndexOf(UNDERSCORE);
        if (dotIndex > 2) {
            return accession.substring(0, dotIndex);
        } else {
            return accession;
        }
    }

    private String cleanDot(String accession) {
        int dotIndex = accession.lastIndexOf(DOT);
        if (dotIndex > 3) {
            return accession.substring(0, dotIndex);
        } else {
            return accession;
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getAccessionColumn() {
        return accessionColumn;
    }

    public void setAccessionColumn(int accessionColumn) {
        this.accessionColumn = accessionColumn;
    }

    public String[] getIncludePatterns() {
        return includePatterns;
    }

    public void setIncludePatterns(String[] includePatterns) {
        this.includePatterns = includePatterns;
    }

    public String[] getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(String[] excludePatterns) {
        this.excludePatterns = excludePatterns;
    }
}
