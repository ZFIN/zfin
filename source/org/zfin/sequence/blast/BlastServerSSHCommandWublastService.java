package org.zfin.sequence.blast;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.log4j.Logger;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.blast.presentation.XMLBlastBean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @deprecated No longer used.  Use MountedWublastBlastService instead.
 */
public final class BlastServerSSHCommandWublastService extends BlastServerSGEWublastService {

    private static final Logger logger = Logger.getLogger(BlastServerSSHCommandWublastService.class);


    private static BlastServerSSHCommandWublastService instance;

    protected BlastServerSSHCommandWublastService() {
    }

    public static BlastServerSSHCommandWublastService getInstance() {
        if (instance == null) {
            instance = new BlastServerSSHCommandWublastService();
        }
        return instance;
    }

    @Override
    public String blastOneDBToString(XMLBlastBean xmlBlastBean) throws BlastDatabaseException, BusException {
        throw new BlastDatabaseException("blast not implemented ") ;
    }

    @Override
    public String blastOneDBToString(XMLBlastBean xmlBlastBean, Database database) throws BlastDatabaseException, BusException {
        throw new BlastDatabaseException("blast not implemented ") ;
    }

    /**
     * In this sense, the key is the same
     *
     * @return String of commands.
     */
    protected List<String> getPrefixCommands() {
        if (prefixCommands.size() == 0) {
            prefixCommands.add(ZfinPropertiesEnum.SSH.value());
            prefixCommands.add(ZfinProperties.getBlastServerUserAtHost());
            prefixCommands.add("-i");
        }
        return prefixCommands;
    }

    @Override
    public String getKeyPath() {
        return ZfinPropertiesEnum.WEBHOST_KEY_PATH + "/";    //To change body of overridden methods use File | Settings | File Templates.
    }


    /**
     * Sends a fasta file over to the remote server for processing in the case where streams can
     * not be used.  On genomix, can not scp to /tmp, because qrsh processes can not read the files
     * there.
     *
     * @param fastaFile File to send.
     * @return The remote file name and location.
     * @throws java.io.IOException Thrown in fasta file not found or secure copy fails.
     */
    @Override
    protected File sendFASTAToServer(File fastaFile, int sliceNumber) throws IOException {
        CommandLine commandLine = new CommandLine("scp");
        commandLine.addArgument(fastaFile.getAbsolutePath());

        File remoteFile = generateFileName(fastaFile, sliceNumber);
        commandLine.addArgument(ZfinProperties.getBlastServerUserAtHost() + ":" + remoteFile.getAbsolutePath());

        DefaultExecutor defaultExecutor = new DefaultExecutor();
        int returnValue = defaultExecutor.execute(commandLine);

        logger.debug("return value: " + returnValue);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream byteArrayErrorStream = new ByteArrayOutputStream();
        logger.debug("output stream: " + byteArrayOutputStream.toString().trim());
        logger.debug("error stream: " + byteArrayErrorStream.toString().trim());

        return remoteFile;
    }

}
