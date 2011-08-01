package org.zfin.datatransfer.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;

/**
 * This class helps download ftp files.
 */
@Service
public class DownloadService {

    private Logger logger = Logger.getLogger(DownloadService.class);

    protected final String ANONYMOUS_USERNAME = "anonymous";
    protected final String ANONYMOUS_PASSWORD = "zfinadmn@zfin.org";
    private int FTP_TIMEOUT = 3000; // ftp timeout in milliseconds
    private final int BUFFER_SIZE = 1024;


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
            return downloadFileHttp(localFile, remoteFile, isGzip);
        }

    }

    /**
     * Checks to see if there is a newer file and downloads and decompresses.
     *
     * @return The location of the local file.
     */
    protected File downloadFileFtp(File localFile, URL remoteFile, boolean useExistingFile, boolean isGzip) {
        try {
            FTPClient ftpClient = new FTPClient();
            ftpClient.setDataTimeout(FTP_TIMEOUT);
            ftpClient.connect(remoteFile.getHost());
            ftpClient.setBufferSize(BUFFER_SIZE);
            ftpClient.login(ANONYMOUS_USERNAME, ANONYMOUS_PASSWORD);

            ftpClient.changeWorkingDirectory(remoteFile.getPath());

            ftpClient.enterLocalPassiveMode();
            FTPFile[] ftpFiles = ftpClient.listFiles(remoteFile.getPath());

            if (ftpFiles.length != 1) {
                String errorText = "Problem retrieving file from server of name: " +
                        remoteFile.toExternalForm();
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

    protected File downloadFileHttp(File localFile, URL remoteFile, boolean isGzip) {
        try {
            logger.info(remoteFile.getUserInfo()) ;
            long remoteFileSize = 100 ; // this file is gzipped
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


            long startTime = System.currentTimeMillis();
            InputStream inputStream ;
            if(isGzip){
                inputStream = new GZIPInputStream(remoteFile.openStream());
            }
            else{
                inputStream = remoteFile.openStream();
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
}
