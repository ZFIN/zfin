package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.exec.CommandLine;
import org.zfin.properties.ZfinProperties;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.framework.exec.ExecProcess;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class relies on genomix mounting a specific HE directory.  For the most part
 */
public final class MountedWublastBlastService extends WebHostWublastBlastService{

    private static final Logger logger = Logger.getLogger(MountedWublastBlastService.class) ;

    private static MountedWublastBlastService instance ;

    public static MountedWublastBlastService getInstance() {
        if (instance==null){
            instance = new MountedWublastBlastService();
        }
        return instance ;
    }


    /**
     * Sends a fasta file over to the remote server for processing in the case where streams can
     * not be used.  On genomix, can not scp to /tmp, because qrsh processes can not read the files
     * there.
     * @param fastaFile File to send.
     * @param sliceNumber Slice number.
     * @return The remote file name and location.
     * @throws java.io.IOException Fails to send fasta file.
     * // todo: this needs to be reimplemented to copy to the correct directory and then execute the
     * // ssh file
     */
    @Override
    protected File sendFASTAToServer(File fastaFile,int sliceNumber) throws IOException{
        
        // file is already written
        // just replace the name here
        File remoteFile = new File(ZfinProperties.getBlastServerDatabasePath()+"/"+fastaFile.getName()) ;

        // execute forced ssh copy from mounted to qrsh_available shared (BLASTSERVER . . . )
        List<String> commandList = new ArrayList<String>() ;
        commandList.add(ZfinProperties.getBlastServerAccessBinary());
        commandList.add(ZfinProperties.getBlastServerUserAtHost()) ;
        commandList.add("-i") ;
        commandList.add(ZfinProperties.getKeyPath()+"/"+"cp");
        commandList.add(fastaFile.getAbsolutePath()) ;
        commandList.add(remoteFile.getAbsolutePath()) ;

        ExecProcess execProcess = new ExecProcess(commandList) ;
        logger.info(execProcess);
        try {
            int returnValue = execProcess.exec();
            logger.debug("return value: "+ returnValue);
        } catch (Exception e) {
            logger.error("Failed to copy file to the server: "+e);
            throw new RuntimeException("failed to send file",e) ;
        }
        logger.debug("output stream: "+ execProcess.getStandardOutput().trim());
        logger.debug("error stream: "+ execProcess.getStandardError().trim());

        return remoteFile ;
    }


    @Override
    /**
     * This is the "blastn" implementation
     * @param xmlBlastBean Blast parameters
     * @return XML as String.
     * @throws org.zfin.sequence.blast.BlastDatabaseException
     */
    public String blastOneDBToString(XMLBlastBean xmlBlastBean,Database database) throws BlastDatabaseException {

        List<String> commandLine = new ArrayList<String>() ;

        try {
            commandLine.add(ZfinProperties.getBlastServerAccessBinary()) ;
            commandLine.add(ZfinProperties.getBlastServerUserAtHost()) ;
            commandLine.add("-i") ;
            commandLine.add(ZfinProperties.getKeyPath()+"/"+xmlBlastBean.getProgram()) ;


            // handle database
            commandLine.add(database.getCurrentBlastServerDatabasePath().trim());

            // set result file if needed
            setBlastResultFile(xmlBlastBean) ;


            // handle sequence here
            // create sequence dump
            // need to prepend a defline so that blast works properly
            String querySequence = xmlBlastBean.getQuerySequence() ;
            // fix defline
            if (querySequence!=null && false==querySequence.startsWith(">")){
                querySequence = ">query: http://zfin.org/action/blast/blast-view?resultFile="
                        + xmlBlastBean.getResultFile().getName() + "\n" +
                        querySequence ;
            }

            // handle poly-a
            if(xmlBlastBean.getPoly_a()!=null && xmlBlastBean.getPoly_a()){
                querySequence = clipPolyATail(querySequence) ;
            }

            // handle query from  / to and dump sequence to fasta
            File fastaSequenceFile = dumpFastaSequence(querySequence ,
                    (xmlBlastBean.getQueryFrom()!=null ? xmlBlastBean.getQueryFrom() : -1 )
                    ,
                    (xmlBlastBean.getQueryTo()!=null ? xmlBlastBean.getQueryTo() : -1 )
            ) ;
            fixFilePermissions(fastaSequenceFile) ;

            File remoteFASTAFile = sendFASTAToServer(fastaSequenceFile,xmlBlastBean.getSliceNumber()) ;
            commandLine.add(remoteFASTAFile.getAbsolutePath());


            // add expect value
            if(xmlBlastBean.getExpectValue()!=null){
                commandLine.add("-e");
                commandLine.add(xmlBlastBean.getExpectValue().toString());
            }

            // word size
            if(xmlBlastBean.getWordLength()!=null){
                commandLine.add("-w");
                commandLine.add(xmlBlastBean.getWordLength().toString());
            }


            // create alignment view
            commandLine.add("-mformat");
            commandLine.add(XMLBlastBean.View.XML.getValue());

            if(StringUtils.isNotEmpty(xmlBlastBean.getMatrix())){
                commandLine.add("-matrix");
                commandLine.add(xmlBlastBean.getMatrix());
            }

            // set the filter for the sequences
            String filter = null ; // default is false filtering
            if(xmlBlastBean.getProgram().equals(XMLBlastBean.Program.BLASTN.getValue())
                    ){
                if(xmlBlastBean.getDust()){
                    filter = FILTER_DUST;
                }
            }
            else{
                if(xmlBlastBean.getSeg()&& xmlBlastBean.getXnu()){
                    filter = FILTER_SEG +"+"+ FILTER_XNU ;
                }
                else
                if(xmlBlastBean.getSeg()){
                    filter = FILTER_SEG ;
                }
                else
                if(xmlBlastBean.getXnu()){
                    filter = FILTER_XNU ;
                }
            }

            if(filter!=null){
                commandLine.add("-filter");
                commandLine.add(filter) ;
            }


            // if distributed, then we use these options
            if(xmlBlastBean.getNumChunks()>1){
                commandLine.add("-dbslice") ;
                commandLine.add( (xmlBlastBean.getSliceNumber()+1)+"/"+(xmlBlastBean.getNumChunks())) ;
            }

            logger.info("remote blast command list: "+ commandLine);
            ExecProcess execProcess = new ExecProcess(commandLine) ;
            try {
                int returnValue = execProcess.exec();
                logger.debug("return value: "+ returnValue);
            } catch (Exception e) {
                logger.warn("Error blasting: "+e);
                throw new RuntimeException("Failed to blast",e) ;
            }
            logger.debug("output stream: "+ execProcess.getStandardOutput().trim());
            logger.debug("error stream: "+ execProcess.getStandardError().trim());

            return fixBlastXML(execProcess.getStandardOutput().trim(),xmlBlastBean) ;
        } catch (Exception e) {
            throw new BlastDatabaseException("failed to blast database with: "+commandLine+e);
        }
    }

    private void fixFilePermissions(File fastaSequenceFile) {
        List<String> commandLine = new ArrayList<String>() ;
        commandLine.add("chmod") ;
        commandLine.add("664") ;
        commandLine.add(fastaSequenceFile.getAbsolutePath()) ;
        ExecProcess execProcess = new ExecProcess(commandLine) ;
        logger.warn(commandLine);
        logger.warn(execProcess);
        try {
            int returnValue = execProcess.exec();
            logger.debug("return value: "+ returnValue);
        } catch (Exception e) {
            logger.warn("Error blasting: "+e);
            throw new RuntimeException("Failed to blast",e) ;
        }
        logger.debug("output stream: "+ execProcess.getStandardOutput().trim());
        logger.debug("error stream: "+ execProcess.getStandardError().trim());
    }


}
