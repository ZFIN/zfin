package org.zfin.sequence.blast;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.log4j.Logger;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.sequence.Sequence;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public abstract class BlastServerSGEWublastService extends AbstractWublastBlastService {

    private static final Logger logger = Logger.getLogger(BlastServerSGEWublastService.class);


    protected List<String> getPrefixCommands() {
        List<String> prefixCommands = getPrefixCommands();
        if (prefixCommands.size() == 0) {
            prefixCommands.add(ZfinPropertiesEnum.SSH.value());
            prefixCommands.add(ZfinProperties.getBlastServerUserAtHost());
            // I don't think that this needs to be queued, but it couldn't hurt
            prefixCommands.add("qrsh");
            prefixCommands.add("-now");
            prefixCommands.add("n");
        }
        return prefixCommands;
    }

    @Override
    public String getCurrentDatabasePath(Database database) {
        return database.getCurrentBlastServerDatabasePath();
    }

    @Override
    public String getBlastGetBinary() {
        return getKeyPath() + ZfinPropertiesEnum.WEBHOST_XDGET;
    }

    @Override
    public String getBlastPutBinary() {
        return getKeyPath() + ZfinPropertiesEnum.WEBHOST_XDFORMAT;
    }

    @Override
    protected DatabaseStatisticsCache getDatabaseStaticsCache() {
        return BlastServerDatabaseStatisticsCache.getInstance();
    }

    // adders
    // todo: fix this method so that it works with remote databases

    public Sequence addSequence(Sequence sequence) throws BlastDatabaseException {
        throw new BlastProtocolNotImplementedException();
    }

    @Override
    protected File dumpDatabaseAsFastaForAccessions(Database blastDatabase, File accessionFile) throws IOException {
        throw new BlastProtocolNotImplementedException();
    }

    @Override
    protected void createDatabaseFromFasta(Database database, File fastaFile) throws BlastDatabaseException {
        throw new BlastProtocolNotImplementedException();
    }

    @Override
    protected Set<String> getDatabaseAccessionsFromList(Database blastDatabase, File validAccessionFile) {
        throw new BlastProtocolNotImplementedException();
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

    @Override
    // todo: think of a better way to handle this
    protected void createEmptyDatabase(Database blastDatabase) throws BlastDatabaseException {
        throw new BlastProtocolNotImplementedException();
    }

    @Override
    // todo: think of a better way to handle this
    protected void appendDatabase(Database oldDatabase, Database newDatabase) throws BlastDatabaseException {
        throw new BlastProtocolNotImplementedException();
    }


    protected File generateFileName(File fastaFile, int sliceNumber) throws IOException {
        return new File(ZfinPropertiesEnum.BLASTSERVER_DISTRIBUTED_QUERY_PATH + "/" + (sliceNumber >= 0 ? sliceNumber + "/" : "") + fastaFile.getName());
    }

    public List<File> backupDatabase(Database database) throws IOException {
        throw new BlastProtocolNotImplementedException();
    }

    public List<File> restoreDatabase(Database database) throws IOException {
        throw new BlastProtocolNotImplementedException();
    }
}