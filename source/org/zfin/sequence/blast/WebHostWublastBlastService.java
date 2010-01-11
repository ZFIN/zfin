package org.zfin.sequence.blast;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Logger;
import org.zfin.framework.exec.ExecProcess;
import org.zfin.gwt.marker.ui.SequenceValidator;
import org.zfin.properties.ZfinProperties;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.Sequence;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebHostWublastBlastService extends AbstractWublastBlastService {

    private static final Logger logger = Logger.getLogger(WebHostWublastBlastService.class);

    private static WebHostWublastBlastService instance;

    public static WebHostWublastBlastService getInstance() {
        if (instance == null) {
            instance = new WebHostWublastBlastService();
        }
        return instance;
    }

    @Override
    public List<String> getPrefixCommands() {
        return prefixCommands;
    }

    @Override
    public String getCurrentDatabasePath(Database database) {
        return database.getCurrentWebHostDatabasePath();
    }

    @Override
    public String getBlastGetBinary() {
        return ZfinProperties.getWebHostBlastGetBinary();
    }

    @Override
    public String getBlastPutBinary() {
        return ZfinProperties.getWebHostBlastPutBinary();
    }

    @Override
    protected DatabaseStatisticsCache getDatabaseStaticsCache() {
        return WebHostDatabaseStatisticsCache.getInstance();
    }

    // adders

    public Sequence addSequence(Sequence sequence) throws BlastDatabaseException {
        Database database = sequence.getDbLink().getReferenceDatabase().getPrimaryBlastDatabase();
        String blastDBPath = getCurrentDatabasePath(database);
        char type = database.getTypeCharacter();
        logger.debug("adding sequence: " + blastDBPath + " type: " + type);

        if (database.getType().isNucleotide() &&
                SequenceValidator.NOT_FOUND != SequenceValidator.validateNucleotideSequence(sequence.getData())
                ) {
            throw new BlastDatabaseException("A bad nucleotide sequence: " + sequence.getData());
        } else if (database.getType().isProtein() &&
                SequenceValidator.NOT_FOUND != SequenceValidator.validatePolypeptideSequence(sequence.getData())
                ) {
            throw new BlastDatabaseException("A bad polypeptide sequence: " + sequence.getData());
        } else if (database.getType().isNucleotide() == false && database.getType().isProtein() == false) {
            throw new BlastDatabaseException("Type not found: " + database.getType());
        }


        try {
            backupDatabase(database);
            getLock(database);
            File fastFilePath = File.createTempFile("fasta", ".fa");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fastFilePath));
            bufferedWriter.write(sequence.getFormattedData());
            bufferedWriter.close();
            List<String> commandLine = new ArrayList<String>();
            commandLine.addAll(getPrefixCommands());
            commandLine.add(getBlastPutBinary());
            commandLine.add("-" + type);
            commandLine.add("-a");
            commandLine.add(blastDBPath);  // vega transcript)
            commandLine.add(fastFilePath.getAbsolutePath());

            logger.info("addSequence exec string: " + commandLine);
            ExecProcess execProcess = new ExecProcess(commandLine);

            int returnValue = execProcess.exec(commandLine);
            logger.debug("return value: " + returnValue);
            logger.debug("output stream: " + execProcess.getStandardOutput().trim());
            logger.debug("error stream: " + execProcess.getStandardError().trim());

            if (execProcess.getStandardError().trim().length() > 0) {
                logger.debug("Failed to xdformat: " + execProcess.getStandardError().toString().trim());
            }

            if (execProcess.getStandardOutput().toString().trim().length() > 0) {
                logger.debug("sequence add reply: " + execProcess.getStandardOutput().toString().trim());
            }
            return sequence;
        }
        catch (Exception e) {
            logger.fatal("Failed to add sequence:", e);
            try {
                restoreDatabase(database);
            } catch (IOException e1) {
                throw new BlastDatabaseException("Failed to restore database[" + database.getAbbrev() + "]", e);
            }
            throw new BlastDatabaseException("Failed to add sequence", e);
        }
        finally {
            unlockForce(database);
        }
    }

    @Override
    protected File dumpDatabaseAsFastaForAccessions(Database blastDatabase, File accessionFile) throws IOException {
        if (blastDatabase == null) {
            logger.error("failed to define primary blast database: " + blastDatabase);
            return null;
        }

        try {
            getLock(blastDatabase, false);
//            List<String> commandList = new ArrayList<String>();
//            commandList.addAll(getPrefixCommands());
//            commandList.add(getBlastGetBinary());
//            commandList.add("-"+(blastDatabase.getTypeCharacter() ) );
//            commandList.add("-f");
//            commandList.add(blastDatabase.getCurrentWebHostDatabasePath());
//            commandList.add(accessionFile.getAbsolutePath());

            CommandLine commandLine = new CommandLine(getBlastGetBinary());
            commandLine.addArgument("-" + (blastDatabase.getTypeCharacter()));
            commandLine.addArgument("-f");
            commandLine.addArgument(blastDatabase.getCurrentWebHostDatabasePath());
            commandLine.addArgument(accessionFile.getAbsolutePath());


            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream byteArrayErrorStream = new ByteArrayOutputStream();
            DefaultExecutor defaultExecutor = new DefaultExecutor();
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(byteArrayOutputStream, byteArrayErrorStream);
            defaultExecutor.setStreamHandler(pumpStreamHandler);
            try {
                int returnValue = defaultExecutor.execute(commandLine);
            } catch (IOException e) {
                logger.warn("problem with exec, may still have worked: " + commandLine, e);
            }

//            ExecProcess execProcess = new ExecProcess(commandList) ;
//            logger.info("dumpDatabaseAsFastaForAccessions exec string: " + execProcess);
//
//            execProcess.exec();

            // dump output
            String errorOutput = new String();
            for (String line : byteArrayErrorStream.toString().split("\n")) {
//                for(String line: execProcess.getStandardError().split("\n")){
                if (
                        false == line.startsWith("Not found:")
                                &&
                                false == line.startsWith("Identifiers not found:")
                                &&
                                false == line.startsWith("FATAL:  Nothing to index!")
                                &&
                                false == line.startsWith("FATAL:  Could not retrieve record no.")
                        ) {
                    errorOutput += line + "\n";
                }
            }

            if (errorOutput.trim().length() > 0) {
                logger.error("Failed to dump sequence correctly: " + errorOutput);
//                throw new IOException("Failed to dump sequences: "+errorOutput.trim()) ;
            }

            File tmpFastaFile = File.createTempFile("tmp", ".fa");
            BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFastaFile));
//            writer.write(execProcess.getStandardOutput().toString());
            writer.write(byteArrayOutputStream.toString());
            writer.close();
            return tmpFastaFile;
        } catch (Exception e) {
            logger.fatal(e);
            return null;
        } finally {
            unlockForce(blastDatabase);
        }

    }

    @Override
    protected void createDatabaseFromFasta(Database database, File fastaFile) throws BlastDatabaseException {
        getLock(database);

        try {
            List<String> commandList = new ArrayList<String>();
            commandList.add(getBlastPutBinary());
            commandList.add("-" + (database.getTypeCharacter()));
            commandList.add("-I");
            commandList.add("-o");
            commandList.add(getCurrentDatabasePath(database));
            commandList.add(fastaFile.getAbsolutePath());

            ExecProcess execProcess = new ExecProcess(commandList);
            logger.info("createDatabaseFromFasta exec string: " + execProcess);
            execProcess.exec();
            logger.debug("std err: " + execProcess.getStandardError());
            logger.debug("std out: " + execProcess.getStandardOutput());

        } catch (Exception e) {
            logger.fatal(e);
            throw new BlastDatabaseException("Failed to create a blast database from database: "
                    + database +
                    " and fastaFile " + fastaFile,
                    e);
        }
        finally {
            unlockForce(database);
        }
    }

    @Override
    protected Set<String> getDatabaseAccessionsFromList(Database blastDatabase, File validAccessionFile) {
        if (blastDatabase == null) {
            logger.error("failed to define primary blast database: " + blastDatabase);
            return null;
        }

        try {
            getLock(blastDatabase, false);

            List<String> commandList = new ArrayList<String>();
            commandList.add(getBlastGetBinary());
            commandList.add("-" + (blastDatabase.getTypeCharacter()));
            commandList.add("-f");
            commandList.add(getCurrentDatabasePath(blastDatabase));
            commandList.add(validAccessionFile.getAbsolutePath());

            ExecProcess execProcess = new ExecProcess(commandList);
            execProcess.exec();
            logger.info("getDatabaseAccessionsFromList exec string: " + execProcess);
            logger.debug("std err: " + execProcess.getStandardError().toString());

            // dump output
            String standardOutput = "";
            Set<String> returnSet = new HashSet<String>();
//            while ((line = stdout.readLine()) != null) {
            for (String line : execProcess.getStandardOutput().split("\n")) {
                if (line.startsWith(">")) {
                    int index1 = line.indexOf("|");
                    int index2 = line.indexOf("|", index1 + 1);
                    returnSet.add(line.substring(index1 + 1, index2));
                }
                standardOutput += line + "\n";
            }
            logger.debug("std out: " + execProcess.getStandardOutput());

            unlockForce(blastDatabase);

            return returnSet;
        } catch (Exception e) {
            logger.fatal(e);
            return null;
        } finally {
            unlockForce(blastDatabase);
        }
    }


    /**
     * Sends a fasta file over to the remote server for processing in the case where streams can
     * not be used.  On genomix, can not scp to /tmp, because qrsh processes can not read the files
     * there.
     *
     * @param fastaFile   File to send.
     * @param sliceNumber Slice number.
     * @return The remote file name and location.
     * @throws java.io.IOException Fails to send fasta file.
     */
    protected File sendFASTAToServer(File fastaFile, int sliceNumber) throws IOException {
        return generateFileName(fastaFile, sliceNumber);
    }


    @Override
    protected void createEmptyDatabase(Database blastDatabase) throws BlastDatabaseException {
        if (blastDatabase == null) {
            logger.error("failed to define primary blast database: " + blastDatabase);
            throw new BlastDatabaseException("failed to define primary blast database: " + blastDatabase);
        }

        getLock(blastDatabase);

        try {
            File tmpEmptyFile = File.createTempFile("empty", "fa");
            logger.debug("file exists: " + tmpEmptyFile.getAbsolutePath());
            List<String> commandList = new ArrayList<String>();
            commandList.add(ZfinProperties.getWebHostBlastPutBinary());
            commandList.add("-" + (blastDatabase.getTypeCharacter()));
            commandList.add("-I");
            commandList.add("-o");
            commandList.add(blastDatabase.getCurrentWebHostDatabasePath());
            commandList.add(tmpEmptyFile.getAbsolutePath());

            ExecProcess execProcess = new ExecProcess(commandList);
            logger.debug("createEmptyDatabase exec string: " + execProcess);
            execProcess.exec();

            logger.debug("error std output: " + execProcess.getStandardError());
            logger.debug("command std output: " + execProcess.getStandardOutput());
            int numberOfSequences = getDatabaseStatistics(blastDatabase).getNumSequences();
            if (numberOfSequences != 0) {
                throw new BlastDatabaseException("the number of blast databases does not equal 0, instead " + numberOfSequences);
            }
        }
        catch (IOException e) {
            logger.fatal(e);
            throw new BlastDatabaseException("failed to create empty database: " + blastDatabase, e);
        }
        catch (InterruptedException e) {
            logger.fatal(e);
            throw new BlastDatabaseException("failed to create empty database: " + blastDatabase, e);
        }
        finally {
            unlockForce(blastDatabase);
        }
    }


    @Override
    protected void appendDatabase(Database oldDatabase, Database newDatabase) throws BlastDatabaseException {

        logger.debug("append onto " + oldDatabase.getName() + " new " + newDatabase.getName());

        if (oldDatabase.getType() != newDatabase.getType()) {
            logger.fatal("Database types do not match: " + oldDatabase + " vs " + newDatabase);
            throw new BlastDatabaseException("Database types do not match: " + oldDatabase + " vs " + newDatabase);
        }


        try {
            getLock(oldDatabase);
            int oldDatabaseSize = getDatabaseStatistics(oldDatabase).getNumSequences();
            // dump out new database to fasta, this is filtered
            Set<String> validNewAccessions = RepositoryFactory.getBlastRepository().getAllValidAccessionNumbers(newDatabase);
            if (validNewAccessions.size() == 0) {
                File newAccessionsFile = newDatabase.getAccessionFile();
                if (newAccessionsFile != null) {
                    validNewAccessions = getAccessionsFromFile(newAccessionsFile);
                }
            }

            if (validNewAccessions.size() == 0) {
                logger.error("no valid accessions for: " + newDatabase);
                return;
            }

            int newDatabaseSize = validNewAccessions.size();
            int combinedDatabaseSize = oldDatabaseSize + newDatabaseSize;
            logger.debug("old size[" + oldDatabaseSize + "] new size[" + newDatabaseSize + "] combined[" + combinedDatabaseSize + "]");

            File accessionFile = createAccessionDump(validNewAccessions, newDatabase);
            File fastaFile = dumpDatabaseAsFastaForAccessions(newDatabase, accessionFile);
            CommandLine commandLine = new CommandLine(ZfinProperties.getWebHostBlastPutBinary());
            commandLine.addArgument("-" + (oldDatabase.getTypeCharacter()));
            commandLine.addArgument("-a");
            commandLine.addArgument(oldDatabase.getCurrentWebHostDatabasePath());
            commandLine.addArgument(fastaFile.getAbsolutePath());
            DefaultExecutor defaultExecutor = new DefaultExecutor();
            int returnValue = defaultExecutor.execute(commandLine);
            if (defaultExecutor.isFailure(returnValue)) {
                logger.error("bad return value: " + returnValue);
            }

            logger.info("appendDatabase exec string: " + commandLine);
//            logger.debug("error std output: " + execProcess.getStandardError());
//            logger.debug("command std output: " + execProcess.getStandardOutput());
            oldDatabaseSize = getDatabaseStatistics(oldDatabase).getNumSequences();
            logger.debug("after append: updated size[" + oldDatabaseSize + "] = combined[" + combinedDatabaseSize + "]");


            // if I have an old set of accessions, then I update those with my new accessions
            File oldAccessionFile = oldDatabase.getAccessionFile();
            Set<String> oldAccessions = getAccessionsFromFile(oldAccessionFile);
            oldAccessions.addAll(validNewAccessions);
            createAccessionDump(oldAccessions, oldDatabase);

        } catch (Exception e) {
            logger.fatal(e);
            throw new BlastDatabaseException("failed to append database[" + newDatabase + "] to [" + oldDatabase + "]", e);
        }
        finally {
            // will execute even with return statement
            unlockForce(oldDatabase);
        }
        // make append command
    }

    @Override
    protected File generateFileName(File fastaFile, int sliceNumber) throws IOException {
        return File.createTempFile(fastaFile.getName(), String.valueOf(sliceNumber));
    }


    public List<File> backupDatabase(final Database database) throws IOException {
        File fromDirectory = new File(ZfinProperties.getWebHostDatabasePath() + "/" + CURRENT_DIRECTORY);
        File toDirectory = new File(ZfinProperties.getWebHostDatabasePath() + "/" + BACKUP_DIRECTORY);
        return copyFiles(fromDirectory, toDirectory, database.getAbbrev().toString());
    }

    public List<File> restoreDatabase(final Database database) throws IOException {
        File fromDirectory = new File(ZfinProperties.getWebHostDatabasePath() + "/" + BACKUP_DIRECTORY);
        File toDirectory = new File(ZfinProperties.getWebHostDatabasePath() + "/" + CURRENT_DIRECTORY);
        return copyFiles(fromDirectory, toDirectory, database.getAbbrev().toString());
    }


    protected List<File> copyFiles(File fromDirectory, File toDirectory, final String filterString) throws IOException {
        logger.debug("backup directory: " + toDirectory);
        logger.debug("current directory: " + fromDirectory);
        File inputFiles[] = fromDirectory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(filterString);
            }
        });
        logger.debug("num files to copy: " + inputFiles.length);
        List<File> outputFiles = new ArrayList<File>();
        for (File inputFile : inputFiles) {
            File outputFile = new File(toDirectory, inputFile.getName());
            outputFiles.add(outputFile);
            logger.debug("reading file: " + inputFile);
            logger.info("copying file: " + outputFile);
            FileChannel source = new FileInputStream(inputFile).getChannel();
            FileChannel destination = new FileOutputStream(outputFile).getChannel();
            destination.transferFrom(source, 0, source.size());
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
        return outputFiles;
    }

}