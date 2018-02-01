package org.zfin.sequence.blast;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.framework.exec.ExecProcess;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.blast.presentation.XMLBlastBean;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class relies on genomix mounting a specific HE directory.  For the most part
 */
public final class MountedWublastBlastService extends WebHostWublastBlastService {

    private static final Logger logger = Logger.getLogger(MountedWublastBlastService.class);

    private static MountedWublastBlastService instance;

    public static MountedWublastBlastService getInstance() {
        if (instance == null) {
            instance = new MountedWublastBlastService();
        }
        return instance;
    }


    @Override
    public String blastOneDBToString(XMLBlastBean xmlBlastBean) throws BlastDatabaseException, BusException {
        if(CollectionUtils.isNotEmpty(xmlBlastBean.getActualDatabaseTargets())){
            return blastOneDBToString(xmlBlastBean,xmlBlastBean.getActualDatabaseTargets().get(0)) ;
        }
        else{
            throw new BlastDatabaseException("Actual database targets not specified for:\n"+ xmlBlastBean) ;
        }
    }

    @Override
    /**
     * This is the "blastn" implementation
     * @param xmlBlastBean Blast parameters
     * @return XML as String.
     * @throws org.zfin.sequence.blast.BlastDatabaseException
     */
    public String blastOneDBToString(XMLBlastBean xmlBlastBean, Database database) throws BlastDatabaseException, BusException {

        List<String> commandLine = new ArrayList<String>();

        try {
            //commandLine.add(ZfinPropertiesEnum.SSH.value());
            //commandLine.add("-x");
            //commandLine.add(ZfinProperties.getBlastServerUserAtHost());
            //commandLine.add("-i");
            //commandLine.add(ZfinPropertiesEnum.WEBHOST_KEY_PATH + "/" + xmlBlastBean.getProgram());
            commandLine.add("nice");
            commandLine.add(ZfinPropertiesEnum.BLASTSERVER_BINARY_PATH + "/" + xmlBlastBean.getProgram());


            // handle database
            commandLine.add(database.getCurrentBlastServerDatabasePath().trim());

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
            fixFilePermissions(fastaSequenceFile);

            //File remoteFASTAFile = sendFASTAToServer(fastaSequenceFile, xmlBlastBean.getSliceNumber());
            //commandLine.add(remoteFASTAFile.getAbsolutePath());
            commandLine.add(fastaSequenceFile.getAbsolutePath());


            // add expect value
            if (xmlBlastBean.getExpectValue() != null) {
                commandLine.add("-e");
                commandLine.add(xmlBlastBean.getExpectValue().toString());
            }

            // word size
            if (xmlBlastBean.getWordLength() != null) {
                commandLine.add("-w");
                commandLine.add(xmlBlastBean.getWordLength().toString());
            }


            // create alignment view
            commandLine.add("-mformat");
            commandLine.add(XMLBlastBean.View.XML.getValue());

            if (StringUtils.isNotEmpty(xmlBlastBean.getMatrix())) {
                commandLine.add("-matrix");
                commandLine.add(xmlBlastBean.getMatrix());
            }

            // set the filter for the sequences
            String filter = null; // default is false filtering
            if (xmlBlastBean.getProgram().equals(XMLBlastBean.Program.BLASTN.getValue())
                    ) {
                if (xmlBlastBean.getDust()) {
                    filter = FILTER_DUST;
                }
            } else {
                if (xmlBlastBean.getSeg() && xmlBlastBean.getXnu()) {
                    filter = FILTER_SEG + "+" + FILTER_XNU;
                } else if (xmlBlastBean.getSeg()) {
                    filter = FILTER_SEG;
                } else if (xmlBlastBean.getXnu()) {
                    filter = FILTER_XNU;
                }
            }

            if (filter != null) {
                commandLine.add("-filter");
                commandLine.add(filter);
            }


            // if distributed, then we use these options
            if (xmlBlastBean.getNumChunks() > 1) {
                commandLine.add("-dbslice");
                commandLine.add((xmlBlastBean.getSliceNumber() + 1) + "/" + (xmlBlastBean.getNumChunks()));
            }

            // Compatibility mode for AB-BLAST 3.0
            commandLine.add("-compat2.0");

            logger.info("remote blast command list: " + commandLine);
            ExecProcess execProcess = new ExecProcess(commandLine);
            int returnValue = -1;
            try {
                xmlBlastBean.setErrorString(null);
                execProcess.setExitValues(WuBlastExitEnum.getNonErrorValues());
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


            // 16, 17, or 23
            if (returnValue > WuBlastExitEnum.BUS_ERROR.getValue()) {
                String standardError = execProcess.getStandardError();
                xmlBlastBean.setErrorString(standardError);
            }
            // bus error
            else if (returnValue == WuBlastExitEnum.BUS_ERROR.getValue()) {
                logger.warn("bus exception for, will we re-run?: " + xmlBlastBean.getTicketNumber());
                xmlBlastBean.setErrorString("Some hits may not be shown due to a system error.  You may wish to resubmit the job.");
            }

            logger.debug("output stream: " + execProcess.getStandardOutput().trim());
            logger.debug("error stream: " + execProcess.getStandardError().trim());

            String returnXML = fixBlastXML(execProcess.getStandardOutput().trim(), xmlBlastBean);

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

    private void fixFilePermissions(File fastaSequenceFile) throws BlastDatabaseException {
        List<String> commandLine = new ArrayList<String>();
        commandLine.add("chmod");
        commandLine.add("664");
        commandLine.add(fastaSequenceFile.getAbsolutePath());
        ExecProcess execProcess = new ExecProcess(commandLine);
        logger.info("command line: " + commandLine);
        logger.debug("exec process: " + execProcess);
        try {
            int returnValue = execProcess.exec();
            logger.debug("return value: " + returnValue);
        } catch (Exception e) {
            String errorString = "Failed to fix file permissions\n";
            errorString += "command line:[" + commandLine.toString().replaceAll(",", " ") + "]\n";
            if (execProcess != null) {
                errorString += "output stream[" + execProcess.getStandardOutput() + "]\n";
                errorString += "error stream[" + execProcess.getStandardError() + "]\n";
            }
            throw new BlastDatabaseException(errorString, e);
        }
        logger.debug("output stream: " + execProcess.getStandardOutput().trim());
        logger.debug("error stream: " + execProcess.getStandardError().trim());
    }

}
