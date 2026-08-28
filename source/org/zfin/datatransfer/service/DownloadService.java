package org.zfin.datatransfer.service;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.datatransfer.persistence.LoadFileLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * This class helps download ftp files.
 */
@Service
public class DownloadService {

    private Logger logger = LogManager.getLogger(DownloadService.class);

    protected final String ANONYMOUS_USERNAME = "anonymous";
    protected final String ANONYMOUS_PASSWORD = "zfinadmn@zfin.org";
    private int FTP_TIMEOUT = 3000; // ftp timeout in milliseconds
    private final int BUFFER_SIZE = 1024;
    private final int HTTP_CONNECT_TIMEOUT = 60000;
    private final int HTTP_READ_TIMEOUT = 60000;


    public File downloadFile(File localFile, URL remoteFile, boolean useExistingFile) {
        if(localFile==null || remoteFile == null){
            logger.error("local file ["+localFile+"] and remote file ["+remoteFile+"] must not be null.");
        }
        boolean  isGzip = false ;
        if(remoteFile.getPath().endsWith("gz")){
            isGzip = true ;
        }
        if(remoteFile.getProtocol().startsWith("ftp")){
            return downloadFileFtp(localFile, remoteFile, useExistingFile, isGzip);
        }
        else{
            return downloadFileHttp(localFile, remoteFile, isGzip, false);
        }

    }

    /**
     * Download remoteFile to localFile over http, but skip the transfer when the copy already on
     * disk still matches the remote one -- see {@link #localFileMatchesRemote}.
     * <p>
     * The bytes are stored verbatim: unlike {@link #downloadFile} this does not decompress a .gz
     * url on the fly, so the local file stays directly comparable to the remote Content-Length and
     * callers keep the archive they can gunzip themselves. Callers that want the decompressed
     * content should gunzip afterwards, keyed off the archive's timestamp rather than re-running
     * unconditionally.
     *
     * @return the local file, whether it was downloaded or reused
     */
    public File downloadFileIfChanged(File localFile, URL remoteFile) {
        return downloadFileHttp(localFile, remoteFile, false, true);
    }

    /**
     * Does the file already on disk still match what the server would send?
     * <p>
     * Compares Content-Length and Last-Modified from a HEAD request. Both are compared when
     * available and both have to agree: either alone is too weak. Size alone misses a same-size
     * revision, and timestamp alone misses the case where the local copy was written recently but
     * from an older release -- which is exactly what happens behind a moving pointer like
     * Ensembl's current_fasta, where the filename never changes between releases.
     * <p>
     * Anything unexpected -- no headers to compare, a non-200, an IO failure -- returns false so
     * the caller downloads. Reusing a stale file is the failure mode worth avoiding here; a
     * needless download only costs time.
     */
    private boolean localFileMatchesRemote(File localFile, URL remoteFile, boolean isGzip) {
        if (!localFile.exists() || localFile.length() == 0) {
            return false;
        }
        HttpURLConnection connection = null;
        try {
            URLConnection urlConnection = remoteFile.openConnection();
            if (!(urlConnection instanceof HttpURLConnection)) {
                return false;
            }
            connection = (HttpURLConnection) urlConnection;
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
            connection.setReadTimeout(HTTP_READ_TIMEOUT);
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                logger.info("HEAD returned " + connection.getResponseCode() + " for " + remoteFile
                        + ", downloading rather than reusing " + localFile.getName());
                return false;
            }

            long remoteLastModified = connection.getLastModified(); // 0 when absent
            long remoteLength = connection.getContentLengthLong();  // -1 when absent

            // when the caller asked us to gunzip on the fly the local file holds the decompressed
            // bytes, so its length cannot be compared against the compressed Content-Length
            boolean lengthComparable = !isGzip && remoteLength >= 0;
            boolean timestampComparable = remoteLastModified > 0;
            if (!lengthComparable && !timestampComparable) {
                logger.info("No Content-Length or Last-Modified from " + remoteFile
                        + ", cannot tell whether " + localFile.getName() + " is current -- downloading");
                return false;
            }
            if (lengthComparable && localFile.length() != remoteLength) {
                logger.info("Remote " + remoteFile + " is " + remoteLength + " bytes but local "
                        + localFile.getName() + " is " + localFile.length() + " -- downloading");
                return false;
            }
            if (timestampComparable && localFile.lastModified() < remoteLastModified) {
                logger.info("Remote " + remoteFile + " modified " + new Date(remoteLastModified)
                        + ", local " + localFile.getName() + " is older (" + new Date(localFile.lastModified())
                        + ") -- downloading");
                return false;
            }
            logger.info("Remote " + remoteFile + " unchanged, reusing " + localFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            logger.warn("Could not check whether " + remoteFile + " changed, downloading instead", e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Checks to see if there is a newer file and downloads and decompresses.
     *
     * @return The location of the local file.
     */
    protected File downloadFileFtp(File localFile, URL remoteFile, boolean useExistingFile, boolean isGzip) {
        try {
            FTPClient ftpClient = getFtpClient(remoteFile);
            FTPFile ftpFile = getSingleFTPFile(remoteFile, ftpClient);
            long remoteFileSize = ftpFile.getSize();

            long localFileSize = localFile.length(); // ths file is not gzipped

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
            // if the local file timestamp is older than the new one
            // ||
            // if the local file does not exist
            // ||
            // if the local file is smaller than the remote file (loads almost always increase in size)
            if ((localFile.exists() && localFile.lastModified() < ftpFile.getTimestamp().getTimeInMillis()
                    && false == useExistingFile)
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
                InputStream gzipInputStream ;
                if(isGzip){
                    gzipInputStream = new GZIPInputStream(ftpClient.retrieveFileStream(remoteFile.getFile()));
                }
                else{
                    gzipInputStream = ftpClient.retrieveFileStream(remoteFile.getFile());
                }
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
                logger.info("Newer file not detected and the local file exists and is of appropriate size: " + localFile.getName());
            }

            ftpClient.logout();
            return localFile;
        } catch (SocketException socketException) {
            logger.error("Failed to connect for file, will try to use existing file: " + localFile.getName(), socketException.fillInStackTrace());
            if (localFile.exists()) {
                logger.warn("File not updated, using existing file: " + localFile.getName());
                return localFile;
            } else {
                logger.error("No file exists, so nothing to update" + localFile.getName());
                throw new RuntimeException("stop parsing, bad IO connection and file not " +
                        "downloaded and no file exists", socketException);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file properly, bailing out of load for file: " + localFile.getName(), e);
        }
    }

    protected File downloadFileHttp(File localFile, URL remoteFile, boolean isGzip, boolean reuseIfUnchanged) {
        try {
            if (reuseIfUnchanged && localFileMatchesRemote(localFile, remoteFile, isGzip)) {
                return localFile;
            }
            logger.info(remoteFile.getUserInfo()) ;
            long remoteFileSize = 100 ; // this file is gzipped
            //long localFileSize = localFile.length(); // ths file is not
            long localFileSize =0;

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


            long startTime = System.currentTimeMillis();
            URLConnection connection = remoteFile.openConnection();
            connection.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
            connection.setReadTimeout(HTTP_READ_TIMEOUT);
            InputStream inputStream ;
            if(isGzip){
                inputStream = new GZIPInputStream(connection.getInputStream());
            }
            else{
                inputStream = connection.getInputStream();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(localFile);

            try {
                IOUtils.copy(inputStream,fileOutputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }

            long endTime = System.currentTimeMillis();
            int downloadTime = (int) (endTime - startTime) / 1000;
            double downloadRate = (double) remoteFileSize / (1024 * 1024) / downloadTime;
            logger.info("finished download to: " + localFile);
            logger.info("Time: " + downloadTime + "(s) Rate: " + downloadRate + " Mb/s");

            inputStream.close();
            fileOutputStream.close();

            // stamp the local copy with the server's Last-Modified so the next run can compare the
            // two directly. Without this the local timestamp is the download time, which is always
            // newer than the remote one and would make a stale file look current.
            long remoteLastModified = connection.getLastModified();
            if (remoteLastModified > 0) {
                localFile.setLastModified(remoteLastModified);
            }

            return localFile;
        } catch (SocketException socketException) {
            logger.error("Failed to connect for file, will try to use existing file: " + localFile.getName(), socketException.fillInStackTrace());
            if (localFile.exists()) {
                logger.warn("File not updated, using existing file: " + localFile.getName());
                return localFile;
            } else {
                logger.error("No file exists, so nothing to update" + localFile.getName());
                throw new RuntimeException("stop parsing, bad IO connection and file not " +
                        "downloaded and no file exists", socketException);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file properly, bailing out of load for file: " + localFile.getName(), e);
        }
    }

    public long fileSizeFtp(URL remoteFile) {
        try {
            FTPClient ftpClient = getFtpClient(remoteFile);
            FTPFile ftpFile = getSingleFTPFile(remoteFile, ftpClient);

            long remoteFileSize = ftpFile.getSize(); // this file is gzipped

            ftpClient.logout();
            return remoteFileSize;
        } catch (SocketException socketException) {
            logger.error("Failed to connect for file size" );
            throw new RuntimeException(socketException);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Date fileDateFtp(URL remoteFile) {
        try {
            FTPClient ftpClient = getFtpClient(remoteFile);
            FTPFile ftpFile = getSingleFTPFile(remoteFile, ftpClient);

            Date remoteFileDate = ftpFile.getTimestamp().getTime(); // this file is gzipped

            ftpClient.logout();
            return remoteFileDate;
        } catch (SocketException socketException) {
            logger.error("Failed to connect for file size" );
            throw new RuntimeException(socketException);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private FTPClient getFtpClient(URL remoteFile) throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setDataTimeout(FTP_TIMEOUT);
        ftpClient.connect(remoteFile.getHost());
        ftpClient.setBufferSize(BUFFER_SIZE);
        ftpClient.login(ANONYMOUS_USERNAME, ANONYMOUS_PASSWORD);
        ftpClient.changeWorkingDirectory(remoteFile.getPath());
        ftpClient.enterLocalPassiveMode();
        return ftpClient;
    }

    private FTPFile getSingleFTPFile(URL remoteFile, FTPClient ftpClient) throws IOException {
        FTPFile[] ftpFiles = ftpClient.listFiles(remoteFile.getPath());

        if (ftpFiles.length != 1) {
            String errorText = "Problem retrieving file from server of name: " +
                    remoteFile.toExternalForm();
            logger.error(errorText);
            throw new RuntimeException(errorText);
        }

        FTPFile ftpFile = ftpFiles[0];
        return ftpFile;
    }


    public static Date getLastModifiedOnServer(URL url) throws IOException {

        if ("ftp".equals(url.getProtocol())) {
            return getDateFromFTPServer(url);
        } else {
            return getDateFromHTTPServer(url);
        }
    }

    /**
     * Get the last modified date of a file on an HTTP server.
     * If none provided, return null
     * @param url
     * @return
     * @throws IOException
     */
    private static Date getDateFromHTTPServer(URL url) throws IOException {
        HttpURLConnection httpCon = null;
        try {
            httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setRequestMethod("HEAD");

            if (httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
                long dateLong = httpCon.getLastModified();

                if (dateLong == 0) {
                    return null;
                }

                return new Date(dateLong);
            } else {
                throw new IOException("Failed to get last modified date. HTTP response code: " + httpCon.getResponseCode());
            }
        } finally {
            if (httpCon != null) {
                httpCon.disconnect();
            }
        }
    }

    private static Date getDateFromFTPServer(URL url) throws IOException {
        return (new DownloadService()).fileDateFtp(url);
    }

    public static long getFileSizeOnServer(URL url) throws IOException {
        if ("ftp".equals(url.getProtocol())) {
            return getFileSizeOnFTPServer(url);
        } else {
            return getFileSizeOnHTTPServer(url);
        }
    }

    private static long getFileSizeOnFTPServer(URL urlString) throws IOException {
        return (new DownloadService()).fileSizeFtp(urlString);
    }

    private static long getFileSizeOnHTTPServer(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        return urlConnection.getContentLengthLong();
    }

    public static void downloadFileViaWget(String url, Path destination, long timeout, Logger log) throws IOException {
        //do some checking on existing file
        //if file exists, check size. Then, either skip download, resume download, or throw exception
        if (destination.toFile().exists()) {
            log.error("File already exists: " + destination);
            long serverSize = getFileSizeOnServer(new URL(url));
            long localSize = destination.toFile().length();
            if (serverSize == localSize) {
                log.info("File sizes match.  Skipping download.");
                return;
            } else if (serverSize > localSize) {
                log.error("Server file is larger than local file.  Downloading anyway assuming resumed download.");
            } else {
                throw new IOException("Server file is smaller than local file.  This should not happen.");
            }
        }

        Map map = new HashMap();
        map.put("destination", destination.toFile());
        map.put("url", url);
        CommandLine cmdLine = new CommandLine("wget");

        //continue download if file already exists (resume)
        cmdLine.addArgument("-c");

        //show progress. show dots every 10MB since these are large files
        cmdLine.addArgument("--progress=dot");
        cmdLine.addArgument("-e");
        cmdLine.addArgument("dotbytes=10M");

        //save to destination
        cmdLine.addArgument("-O");
        cmdLine.addArgument("${destination}");

        //download from url
        cmdLine.addArgument("${url}");
        cmdLine.setSubstitutionMap(map);
        if (log != null) {
            log.info("Running command: " + cmdLine.toString());
        }

        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0);
        if (timeout > 0) {
            ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
            executor.setWatchdog(watchdog);
        }
        executor.execute(cmdLine);
    }

    public static boolean isDownloadAlreadyProcessed(String downloadUrl, GafOrganization.OrganizationEnum organizationEnum) {
        return isDownloadAlreadyProcessed(downloadUrl, organizationEnum, null);
    }

    public static boolean isDownloadAlreadyProcessed(String downloadUrl, GafOrganization.OrganizationEnum organizationEnum, Date lastModified) {
        if (lastModified == null) {
            try {
                lastModified = getLastModifiedOnServer(new URL(downloadUrl));
            } catch (IOException e) {
                throw new RuntimeException("Could not get last modified date from server for url: " + downloadUrl, e);
            }
        }
        String dateAsString = new java.text.SimpleDateFormat("yyyy-MM-dd").format(lastModified);
        LoadFileLog loadFileLog = getInfrastructureRepository().getLoadFileLog(organizationEnum.toString(), dateAsString);
        return loadFileLog != null;
    }

}
