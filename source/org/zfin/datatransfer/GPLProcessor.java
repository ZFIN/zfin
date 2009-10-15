package org.zfin.datatransfer;

import org.zfin.sequence.Accession;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.properties.ZfinProperties;
import org.apache.log4j.Logger;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTP;

import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.MalformedURLException;


public abstract class GPLProcessor {

    private Logger logger = Logger.getLogger(GPLProcessor.class) ;

    // download stuff
    protected String urlString = "ftp.ncbi.nih.gov" ;
    protected String tempDirectory = System.getProperty("java.io.tmpdir",null) ;
    protected String workingDirectory = "pub/geo/DATA/SOFT/by_platform/"+getFileName();  //To change body of implemented methods use File | Settings | File Templates.
    protected String fileNameSuffix = "_family.soft" ;
    protected String gzipSuffix = ".gz" ;
    protected String userName = "anonymous" ;
    protected String password = "zfinadmn@zfin.org" ;
    protected abstract String getFileName() ;


    public abstract Set<String> parseUniqueNumbers() ;
    public abstract Set<Accession> parse() ;


    /**
     * Checks to see if there is a newer file and downloads and decompresses.
     * @return The location of the local file.
     */
    public File downloadFile() {
        try {
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(urlString);
            ftpClient.login(userName,password);

            ftpClient.changeWorkingDirectory(workingDirectory) ;
            FTPFile[] ftpFiles = ftpClient.listFiles(getFileName()+fileNameSuffix+gzipSuffix) ;

            if(ftpFiles.length!=1){
                throw new RuntimeException("Problem retrieving file from NCBI of name: "+ getFileName()+fileNameSuffix+gzipSuffix ) ;
            }

            FTPFile ftpFile = ftpFiles[0] ;
            Date remoteDate = ftpFile.getTimestamp().getTime() ;

            // do we have a local file?
            File localFile = new File(tempDirectory + "/" + getFileName()+fileNameSuffix) ;
            // if the local file does not exist
            // if the local file timestamp is older than the new one
            if(
                    (localFile.exists() && localFile.lastModified()<remoteDate.getTime())
                            ||
                            (false==localFile.exists())
                    ){
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                localFile.createNewFile();
                logger.info("starting to download to: "+ localFile) ;
                FileOutputStream fileOutputStream = new FileOutputStream( localFile);
                GZIPInputStream gzipInputStream = new GZIPInputStream(ftpClient.retrieveFileStream(ftpFile.getName())) ;
                byte[] buf = new byte[1024];
                int len;
                while((len = gzipInputStream.read(buf)) > 0) {
                    fileOutputStream.write(buf, 0, len);
                }
                logger.info("fnished download to: "+ localFile) ;

                gzipInputStream.close();
                fileOutputStream.close();
                ftpClient.logout();
                localFile.setLastModified(remoteDate.getTime()) ;
                return localFile ;
            }
            else{
                logger.info("newer file not detected: "+getFileName()+fileNameSuffix);
            }

            ftpClient.logout();
            return localFile ;
        } catch (Exception e) {
            logger.error(e);
            return null ;
        }
    }


    /**
     * Destructive method.
     * @param accession
     * @return Accession without a .
     */
    public String fixAccession(String accession){
        int dotIndex = accession.indexOf(".") ;
        if( dotIndex>0){
            return accession.substring(0,dotIndex) ;
        }
        else{
            return accession ;
        }
    }


}
