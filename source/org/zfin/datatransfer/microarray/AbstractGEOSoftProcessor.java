package org.zfin.datatransfer.microarray ;

import org.apache.log4j.Logger;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTP;

import java.util.Set;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.io.*;
import java.text.NumberFormat;
import java.net.SocketException;


public abstract class AbstractGEOSoftProcessor implements SoftParser{

    private Logger logger = Logger.getLogger(AbstractGEOSoftProcessor.class) ;

    // parsing stuff stuff
    private int  accessionColumn = 2 ;
    private String[] includePatterns = null ;
    private String[] excludePatterns = null ;
    private String[] defaultAccessionExcludePatterns = new String[]{"XM_","XR_","ENSDART","ETG","NC_","NG_","DRPTA","A_","DCP_"};
    private final static String DOT = "." ;
    private final static String UNDERSCORE = "_" ;

    // download stuff
    private String fileName ;
    protected String urlString = "ftp.ncbi.nih.gov" ;
    protected String tempDirectory = System.getProperty("java.io.tmpdir",null) ;
    protected String workingDirectory ;  //To change body of implemented methods use File | Settings | File Templates.
    protected String fileNameSuffix = "_family.soft" ;
    protected String gzipSuffix = ".gz" ;
    protected String userName = "anonymous" ;
    protected String password = "zfinadmn@zfin.org" ;
    private int FTP_TIMEOUT = 3000 ; // ftp timeout in milliseconds
    private boolean alwaysUseExistingFile = false ;

    public Set<String> parseUniqueNumbers(String fileName, int column) {
        return parseUniqueNumbers(fileName, column,null,null) ;
    }


    public Set<String> parseUniqueNumbers(String fileName, int column,String[] includePattern)  {
        return parseUniqueNumbers(fileName, column,includePattern,null) ;
    }


    public Set<String> parseUniqueNumbers(String fileName, int column,String[] includePattern,String[] excludePattern)  {
        setAccessionColumn(column);
        setFileName(fileName);
        setIncludePatterns(includePattern);
        setExcludePatterns(excludePattern);
        File file = downloadFile() ;
        return parseUniqueNumbers(file) ;
    }

    @Override
    public void setAlwaysUseExistingFile(boolean alwaysUseExistingFile) {
       this.alwaysUseExistingFile = alwaysUseExistingFile ;
    }

    @Override
    public boolean isAlwaysUseExistingFile() {
        return this.alwaysUseExistingFile ;
    }

    /**
     * Checks to see if there is a newer file and downloads and decompresses.
     * @return The location of the local file.
     */
    public File downloadFile() {
        File localFile = new File(tempDirectory + "/" + getFileName()+fileNameSuffix) ;
        try {
            workingDirectory = "pub/geo/DATA/SOFT/by_platform/"+this.fileName ;
            FTPClient ftpClient = new FTPClient();
            ftpClient.setDataTimeout(FTP_TIMEOUT);
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
            logger.info("downloaded file at: "+localFile.getAbsolutePath());
            // if the local file does not exist
            // if the local file timestamp is older than the new one
            if(
                    (localFile.exists() && localFile.lastModified()<remoteDate.getTime() && false==alwaysUseExistingFile)
                            ||
                            (false==localFile.exists())
                    ){
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                localFile.createNewFile();
                logger.info("starting to download to: "+ localFile + " size: "+  NumberFormat.getInstance().format((ftpFile.getSize()/1000000d)) + " Mb" ) ;
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
        }
        catch(SocketException socketException){
            logger.error("Failed to connect for file, will try to use existing file: "+fileName,socketException.fillInStackTrace());
            if(localFile.exists()){
                logger.warn("File not updated, using existing file: "+ fileName);
                return localFile ;
            }
            else{
                logger.error("No file exists, so nothing to update"+ fileName);
                throw new RuntimeException("stop parsing, bad IO connection and file not downloaded and no file exists",socketException);
            }
        }
        catch(Exception e){
            throw new RuntimeException("Failed to download file properly, bailing out of load for file: "+getFileName(),e.fillInStackTrace()) ;
        }
    }

    public Set<String> parseUniqueNumbers(File file) {

        Set<String> accessionNumbers = new HashSet<String>() ;

        BufferedReader reader = null ;
        try{
            reader = new BufferedReader(new FileReader(file)) ;
            String buffer ;

            // read from input file to output file
            boolean startReading = false ;
            int count = 0 ;
            while( (buffer = reader.readLine())!= null ){
                if(buffer.startsWith("!platform_table_end")){
                    logger.info("number of geo accession from "+getFileName()+": "+count);

                    // read first 3 here
                    Iterator<String> iter = accessionNumbers.iterator();
                    for(int i = 0 ; iter.hasNext() ; i++){
//                        for(int i = 0 ; iter.hasNext() && i < 3 ; i++){
                        logger.debug(getFileName() + " col: " + getAccessionColumn() + ": "+iter.next());
                    }
                    return accessionNumbers ;
                }
                else
                if(startReading==true && doInclude(buffer) && doExclude(buffer)){
                    String accessionNumber = parseLine(buffer) ;
                    if(isValidAccessionNumber(accessionNumber)){
                        accessionNumbers.add(accessionNumber) ;
                        ++count ;
                    }
                }
                else
                if(buffer.startsWith("!platform_table_begin")){
                    startReading = true ;
                    // skip header, which they all have
                    reader.readLine() ;
                }
            }

        }
        catch(Exception e){
            logger.error("fail to parse: " + e.fillInStackTrace());
        }
        finally{
            if(file!=null){
                try{
                    reader.close() ;
                }
                catch(Exception e){
                    logger.error("fail to close: " + e.fillInStackTrace());
                }
            }
        }
        return accessionNumbers ;



    }

    private boolean isValidAccessionNumber(String accessionNumber) {
        if(accessionNumber==null) return false ;

        if(defaultAccessionExcludePatterns !=null){
            for(String excludePattern: defaultAccessionExcludePatterns){
                if(accessionNumber.contains(excludePattern)){
                    return false ;
                }
            }
        }
        return true ;
    }

    /**
     *
     * @param buffer String to check.
     * @return True if buffer  does not include either an excluded or defaultExcluded pattern.
     */
    private boolean doExclude(String buffer) {
        if(excludePatterns!=null ){
            for(String excludePattern:excludePatterns){
                if(buffer.contains(excludePattern)){
                    return false ;
                }
            }
        }
        return true ; 
    }

    /**
     * @param buffer String to check.
     * @return True if buffer contains the included pattern.
     */
    private boolean doInclude(String buffer) {
        if(includePatterns==null || includePatterns.length==0){
            return true ;
        }
        for(String includePattern:includePatterns){
            if(false==buffer.contains(includePattern)){
                return false ;
            }
        }
        return true ;
    }

    /**
     * Destructive method.
     * @param accession Accession to fix.
     * @return Accession without a . or _ in the wrong spot.
     */
    public String fixAccession(String accession){
        accession = cleanDot(accession) ;
        accession = cleanUnderscore(accession) ;
        return accession ;
    }

    private String cleanUnderscore(String accession) {
        int dotIndex = accession.lastIndexOf(UNDERSCORE) ;
        if( dotIndex>2){
            return accession.substring(0,dotIndex) ;
        }
        else{
            return accession ;
        }
    }

    private String cleanDot(String accession) {
        int dotIndex = accession.lastIndexOf(DOT) ;
        if( dotIndex>3){
            return accession.substring(0,dotIndex) ;
        }
        else{
            return accession ;
        }
    }

    public String getFileName() {
        return fileName ;
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
