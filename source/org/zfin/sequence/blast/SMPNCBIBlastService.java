package org.zfin.sequence.blast;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.framework.exec.ExecProcess;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.blast.presentation.XMLBlastBean;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class makes the following assumptions:
 * 1 - the blast machine and the sequence machine are the same
 * 2 - the blast machine has access to SMP processors
 * 3 - because it is local, we can use wu-blastall to blast our sequences
 *
 * This should SIGNIFICANTLY simplify code.
 */
public class SMPNCBIBlastService extends WebHostWublastBlastService {

    private static final Logger logger = LogManager.getLogger(SMPNCBIBlastService.class);

    private static final String NCBI_BINARY_PATH = "/usr/local/ncbi/blast/bin" ;

    private static SMPNCBIBlastService instance;

    public static SMPNCBIBlastService getInstance() {
        if (instance == null) {
            instance = new SMPNCBIBlastService();
        }
        return instance;
    }

    @Override
    public String blastOneDBToString(XMLBlastBean xmlBlastBean,Database database) throws BlastDatabaseException, BusException {
        XMLBlastBean xmlBlastBeanCopy = xmlBlastBean.clone();
        List<Database> actualDatabase = new ArrayList<Database>();
        actualDatabase.add(database) ;
        xmlBlastBeanCopy.setActualDatabaseTargets(actualDatabase);
        return blastOneDBToString(xmlBlastBean) ;
    }

    @Override
    /**
     * Should be the wublastall implemenation, ignores database
     * we want it of the form:
     * wu-blastall -p blastn -d "vega_zfin unreleasedRNA" -e 10.0 -m 7  -i ~/fasta4016420893683199371fa
     *
     * @param xmlBlastBean Blast parameters
     * @return XML as String.
     * @throws BlastDatabaseException
     */
    public String blastOneDBToString(XMLBlastBean xmlBlastBean) throws BlastDatabaseException, BusException {

        List<String> commandLine = new ArrayList<String>();

        try {
            commandLine.add(NCBI_BINARY_PATH + "/" +  xmlBlastBean.getProgram()) ;

            // number of processors
            commandLine.add("-num_threads") ;
            int numProcs = xmlBlastBean.getNumChunks();
            if(numProcs<1){
                numProcs = 1 ;
            }
            commandLine.add( String.valueOf(numProcs)) ;

            // databases
            commandLine.add("-db") ;
            // I believe that thesea are comma separated, so just ned to change to space

            commandLine.add(getAbsoluteDatabaseString(xmlBlastBean)) ;
//            commandLine.add(xmlBlastBean.getDataLibraryString().replaceAll(","," ")) ;
            // set result file if needed


            setBlastResultFile(xmlBlastBean);


            // handle sequence here
            // create sequence dump
            // need to prepend a defline so that blast works properly
            String querySequence = xmlBlastBean.getQuerySequence();
            // fix defline
            if (querySequence != null && false == querySequence.startsWith(">")) {
                querySequence = ">query: http://zfin.org/action/blast/blast-view?resultFile="
                        + xmlBlastBean.getResultFile().getName() + "\n" +
                        querySequence;
            }

            // handle poly-a
            if (xmlBlastBean.getPoly_a() != null && xmlBlastBean.getPoly_a()) {
                querySequence = clipPolyATail(querySequence);
            }

            // handle query from  / to and dump sequence to fasta
            File fastaSequenceFile = dumpFastaSequence(querySequence,
                    (xmlBlastBean.getQueryFrom() != null ? xmlBlastBean.getQueryFrom() : -1)
                    ,
                    (xmlBlastBean.getQueryTo() != null ? xmlBlastBean.getQueryTo() : -1)
            );

            // TODO: replace with standard input at some point
            commandLine.add("-query");
            commandLine.add(fastaSequenceFile.getAbsolutePath());


            // add expect value
            if (xmlBlastBean.getExpectValue() != null) {
                commandLine.add("-evalue");
                commandLine.add(xmlBlastBean.getExpectValue().toString());
            }

            // word size
            if (xmlBlastBean.getWordLength() != null) {
                commandLine.add("-word_size");
                commandLine.add(xmlBlastBean.getWordLength().toString());
            }


            // create alignment view
            commandLine.add("-outfmt");
            // this is NCBI's xml format
            commandLine.add("5");

            // need to get these files before we enable this, but . . . 
            if (StringUtils.isNotEmpty(xmlBlastBean.getMatrix())) {
                commandLine.add("-matrix");
                commandLine.add(xmlBlastBean.getMatrix());
            }


            // set the filter for the sequences
            String filter = null; // default is false filtering
            if (xmlBlastBean.getProgram().equals(XMLBlastBean.Program.BLASTN.getValue())
                    ) {
                commandLine.add("-dust") ;
                commandLine.add( xmlBlastBean.getDust() ? "yes" : "no") ;
            } else {
                commandLine.add("-seg") ;
                commandLine.add( xmlBlastBean.getSeg() ? "yes" : "no") ;

                // TODO: can not find a similar complement in NCBI blast
//                commandLine.add("-xnu") ;
//                commandLine.add( xmlBlastBean.getXnu() ? "yes" : "no") ;
            }



            logger.info("blast command list: " + commandLine.toString().replaceAll(","," "));
            ExecProcess execProcess = new ExecProcess(commandLine,false);
            int returnValue = -1;
            try {
                xmlBlastBean.setErrorString(null);
//                execProcess.setExitValues(WuBlastExitEnum.getNonErrorValues());
                returnValue = execProcess.exec();
                logger.debug("return value: " + returnValue);
            } catch (Exception e) {
                String errorString = "";
                errorString += "command line:[" + commandLine.toString().replaceAll(",", " ") + "]\n";
                String standardError = execProcess.getStandardError();
                if (execProcess != null) {
                    errorString += "ticket[" + xmlBlastBean.getTicketNumber() + "]\n";
                    errorString += "output stream[" + execProcess.getStandardOutput() + "]\n";
                    errorString += "error stream[" + standardError + "]\n";
                }


                throw new BlastDatabaseException(xmlBlastBean.getTicketNumber() + ": Failed to blast\n " + errorString, e);
            }


//            // 16, 17, or 23
//            if (returnValue > WuBlastExitEnum.BUS_ERROR.getValue()) {
//                String standardError = execProcess.getStandardError();
//                xmlBlastBean.setErrorString(standardError);
//            }
//            // bus error
//            else if (returnValue == WuBlastExitEnum.BUS_ERROR.getValue()) {
//                logger.warn("bus exception for, will we re-run?: " + xmlBlastBean.getTicketNumber());
//                xmlBlastBean.setErrorString("Some hits may not be shown due to a system error.  You may wish to resubmit the job.");
//            }

            logger.debug("output stream: " + execProcess.getStandardOutput().trim());
            String standardOutput = execProcess.getStandardOutput().trim() ;
            logger.debug("error stream: " + standardOutput);

            String returnXML = fixBlastXML(standardOutput, xmlBlastBean);
            logger.debug( "fixed stream stream:" + returnXML );

            // bus error
            if (returnValue == WuBlastExitEnum.BUS_ERROR.getValue()) {
                throw new BusException("Bus Error for blast:\n" + xmlBlastBean, returnXML);
            }

            return returnXML;

        }
        catch (BusException busException) {
            throw busException;
        }
        catch (Exception e) {
            e.fillInStackTrace();
            String errorString = xmlBlastBean.getTicketNumber() + ": failed to blast database with: " + commandLine.toString().replaceAll(",", " ") + "\n" + e;
            throw new BlastDatabaseException(errorString, e);
        }
    }

    @Override
    protected String fixBlastXML(String returnXML, XMLBlastBean xmlBlastBean) {
        int index = returnXML.indexOf("<BlastOutput>") ;
        returnXML = returnXML.substring(index);
        returnXML = super.fixBlastXML(returnXML, xmlBlastBean);

        return returnXML ;
    }

    protected String getAbsoluteDatabaseString(XMLBlastBean xmlBlastBean){
        Iterator<Database> iter = xmlBlastBean.getActualDatabaseTargets().iterator();
        StringBuilder sb = new StringBuilder("");
        while(iter.hasNext()){
            Database database = iter.next();
            sb.append(ZfinPropertiesEnum.WEBHOST_BLAST_DATABASE_PATH).append("/Current/").append(database.getAbbrev()) ;
            if(iter.hasNext()){
                sb.append(" ") ;
            }
        }
        sb.append("") ;
        return sb.toString() ;
    }


}