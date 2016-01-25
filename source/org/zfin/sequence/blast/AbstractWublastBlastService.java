package org.zfin.sequence.blast;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.exec.ExecProcess;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.sequence.blast.repository.BlastRepository;
import org.zfin.sequence.repository.SequenceRepository;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class
 */
public abstract class AbstractWublastBlastService implements BlastService {

    private final static Logger logger = Logger.getLogger(AbstractWublastBlastService.class);

    public final static String BAD_QUERY_SEARCH = "There are no valid contexts in the requested search.";

    protected List<String> prefixCommands = new ArrayList<>();

    protected final BlastRepository blastRepository = RepositoryFactory.getBlastRepository();

    // timeout constant
    private static final int LOCK_TIMEOUT_MS = 3000;

    protected abstract Sequence addSequence(Sequence sequence) throws BlastDatabaseException;

    protected String keyPath = "";

    private int maxAttempts = 5;
    public static final Pattern nucleotideSequencePattern = Pattern.compile("[ATCGUMKRYVBHDWSNatcgumkryvbhdwsn\\-]{40,}");
    public static final Pattern proteinSequencePattern = Pattern.compile("[LSEFAVGKTRPDIQNFYHMCWBZJXlsefavgktrpdiqnfyhmcwbzjx\\-\\*]{40,}");


    // regeneration methods

    protected abstract File dumpDatabaseAsFastaForAccessions(Database database, File accessionFile) throws IOException;

    protected abstract void createDatabaseFromFasta(Database database, File fastaFile) throws BlastDatabaseException;

    protected abstract Set<String> getDatabaseAccessionsFromList(Database database, File validAccessionFile) throws IOException;

    protected abstract File generateFileName(File fastaFile, int sliceNumber) throws IOException;

    protected abstract File sendFASTAToServer(File fastaFile, int sliceNumber) throws IOException;


    protected abstract String getCurrentDatabasePath(Database database);

    protected abstract String getBlastGetBinary();

    protected abstract String getBlastPutBinary();

    protected abstract DatabaseStatisticsCache getDatabaseStaticsCache();


    // appending methods

    /**
     * This methods obliterats
     *
     * @param database The database to obliterate.
     * @return operation success
     */
    protected abstract void createEmptyDatabase(Database database) throws BlastDatabaseException;

    /**
     * Append a database to the current one.
     *
     * @param oldDatabase The database to append to.
     * @param newDatabase The database that is added.
     * @return operation success
     */
    protected abstract void appendDatabase(Database oldDatabase, Database newDatabase) throws BlastDatabaseException;

    // specific commands

    protected List<String> getPrefixCommands() {
        return prefixCommands;
    }

    public void setPrefixCommands(List<String> prefixCommands) {
        this.prefixCommands = prefixCommands;
    }

    public String getKeyPath() {
        return keyPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    /**
     * @param transcriptZdbID        Zdb for Transcript
     * @param sequenceData           Sequence string
     * @param referenceDatabaseZdbID RefDB for BlastDB
     * @return The returned sequence should also have the appropriately referenced dblink.
     */
    public Sequence addSequenceToTranscript(String transcriptZdbID, String sequenceData, String referenceDatabaseZdbID)
            throws BlastDatabaseException {

        Transcript transcript = RepositoryFactory.getMarkerRepository().getTranscriptByZdbID(transcriptZdbID);
        logger.debug(transcript);
        ReferenceDatabase referenceDatabase = (ReferenceDatabase) HibernateUtil.currentSession().get(ReferenceDatabase.class, referenceDatabaseZdbID);
        logger.debug(referenceDatabase);
        logger.debug("primary blast database: " + referenceDatabase.getPrimaryBlastDatabase());
        Database.Type blastDatabaseType = referenceDatabase.getPrimaryBlastDatabase().getType();
        logger.debug(blastDatabaseType);


        // get a valid accession #

        String generatedAccessionNumber;
        if (blastDatabaseType == Database.Type.NUCLEOTIDE) {
            generatedAccessionNumber = NucleotideInternalAccessionGenerator.getInstance().generateAccession();
        } else {
            generatedAccessionNumber = ProteinInternalAccessionGenerator.getInstance().generateAccession();
        }
        logger.debug(generatedAccessionNumber);


        // create dblink
        TranscriptDBLink transcriptDBLink = new TranscriptDBLink();
        transcriptDBLink.setLength(sequenceData.length());
        transcriptDBLink.setTranscript(transcript);
        transcriptDBLink.setReferenceDatabase(referenceDatabase);
        transcriptDBLink.setAccessionNumber(generatedAccessionNumber);


        logger.debug(transcriptDBLink);

        Session session = HibernateUtil.currentSession();
        session.save(transcriptDBLink);

        // define sequence
        Defline defLine = new TranscriptDefline(transcriptDBLink);


        logger.debug(defLine);


        // create backing accession
        Accession accession = new Accession();
        accession.setNumber(generatedAccessionNumber);
        accession.setReferenceDatabase(referenceDatabase);
        accession.setDefline(defLine.toString());
        accession.setLength(sequenceData.length());
        accession.setAbbreviation(transcript.getAbbreviation());

        session.save(accession);

        // update transcript
        Set<TranscriptDBLink> links = transcript.getTranscriptDBLinks();
        links.add(transcriptDBLink);
        transcript.setTranscriptDBLinks(links);


        // create sequence to add
        Sequence sequence = new Sequence();
        sequence.setData(sequenceData);
        sequence.setDefLine(defLine);
        logger.info("defline: " + defLine);
        sequence.setDbLink(transcriptDBLink);

        return addSequence(sequence);
    }


    public Sequence addProteinToMarker(Marker marker, String sequenceData, String pubZdbID,
                                       ReferenceDatabase referenceDatabase) throws BlastDatabaseException {
        Session session = HibernateUtil.currentSession();
        String generatedAccessionNumber = ProteinInternalAccessionGenerator.getInstance().generateAccession();
        logger.debug("accession #:" + generatedAccessionNumber);


        // write DBLink for marker
        MarkerDBLink markerDBLink = new MarkerDBLink();
        markerDBLink.setLength(sequenceData.length());
        markerDBLink.setMarker(marker);
        markerDBLink.setReferenceDatabase(referenceDatabase);
        markerDBLink.setAccessionNumber(generatedAccessionNumber);
        session.save(markerDBLink);


        // write sequence out
        Defline defLine = new MarkerDefline(markerDBLink);
        Sequence sequence = new Sequence();
        sequence.setData(sequenceData);
        sequence.setDefLine(defLine);
        sequence.setDbLink(markerDBLink);

        Accession accession = new Accession();
        accession.setNumber(markerDBLink.getAccessionNumber());
        accession.setReferenceDatabase(referenceDatabase);
        accession.setDefline(defLine.toString());
        accession.setLength(sequenceData.length());
        accession.setAbbreviation(marker.getAbbreviation());
        session.save(accession);

        session.flush();


        // write attribution out (just one!)
        if (pubZdbID != null && pubZdbID.length() > 0) {
            RepositoryFactory.getInfrastructureRepository().insertRecordAttribution(markerDBLink.getZdbID(), pubZdbID);
        }

        return addSequence(sequence);
    }


    /**
     * @param marker            Marker to add sequence to
     * @param sequenceData      Sequence string
     * @param pubZdbID          Publication to attribute
     * @param referenceDatabase A reference database to set to blast database
     * @return The returned sequence should also have the appropriately referenced dblink.
     */
    public Sequence addSequenceToMarker(Marker marker, String sequenceData, String pubZdbID,
                                        ReferenceDatabase referenceDatabase) throws BlastDatabaseException {

        Session session = HibernateUtil.currentSession();
        String generatedAccessionNumber = NucleotideInternalAccessionGenerator.getInstance().generateAccession();
        logger.info("accession #:" + generatedAccessionNumber);

        // write DBLink for marker
        MarkerDBLink markerDBLink = new MarkerDBLink();
        markerDBLink.setLength(sequenceData.length());
        markerDBLink.setMarker(marker);
        markerDBLink.setReferenceDatabase(referenceDatabase);
        markerDBLink.setAccessionNumber(generatedAccessionNumber);
        session.save(markerDBLink);


        // write sequence out
        Defline defLine = new MarkerDefline(markerDBLink);
        Sequence sequence = new Sequence();
        sequence.setData(sequenceData);
        sequence.setDefLine(defLine);
        sequence.setDbLink(markerDBLink);

        Accession accession = new Accession();
        accession.setNumber(markerDBLink.getAccessionNumber());
        accession.setReferenceDatabase(referenceDatabase);
        accession.setDefline(defLine.toString());
        accession.setLength(sequenceData.length());
        accession.setAbbreviation(marker.getAbbreviation());
        session.save(accession);

        session.flush();

        // write attribution out (just one!)
        if (pubZdbID != null && pubZdbID.length() > 0) {
            RepositoryFactory.getInfrastructureRepository().insertRecordAttribution(markerDBLink.getZdbID(), pubZdbID);
        }

        return addSequence(sequence);
    }


    protected List<Sequence> getSequencesForMarker(Marker marker, DisplayGroup.GroupName... groupNames) throws BlastDatabaseException {
        List<ReferenceDatabase> referenceDatabases = RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(groupNames);
        return getSequencesForMarker(marker, referenceDatabases.toArray(new ReferenceDatabase[referenceDatabases.size()]));
    }


    public List<Sequence> getSequencesForAccessionAndDisplayGroup(String accession, DisplayGroup.GroupName... groupNames) throws BlastDatabaseException {
        SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
        List<ReferenceDatabase> referenceDatabases = RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(groupNames);
        List<DBLink> dbLinks = sequenceRepository.getDBLinks(accession, referenceDatabases.toArray(new ReferenceDatabase[referenceDatabases.size()]));

        List<Sequence> sequences = new ArrayList<>();
        for (DBLink dbLink : dbLinks) {
            sequences.addAll(getSequencesFromSource(dbLink));
        }
        return sequences;
    }


    public void regenerateCuratedDatabases() throws BlastDatabaseException {
        try {
            List<Database> databases = blastRepository.getDatabaseByOrigination(
                    Origination.Type.CURATED);
            for (Database database : databases) {
                regenerateDatabaseFromValidAccessions(database);
            }
        } catch (Exception e) {
            logger.error("Failed to regenerate curated sequences", e);
            throw new BlastDatabaseException("Failed to regenerate CURATED blast databases", e);
        }
    }

    /**
     * This dumps a FASTA sequence for a single sequence.
     *
     * @param fastaSequence The entire sequence.
     * @param start         Start
     * @param finish        Finish
     * @return The file pointing to the sequence.
     * @throws IOException            Failures in file IO.
     * @throws BlastDatabaseException BLAST failures.
     */
    public File dumpFastaSequence(String fastaSequence, int start, int finish) throws IOException, BlastDatabaseException {
        if (false == fastaSequence.startsWith(">")) {
            throw new BlastDatabaseException("dumped fasta sequences must begin with defline: " + fastaSequence);
        }
        File tempFile = File.createTempFile("dump", ".fa", new File(ZfinPropertiesEnum.WEBHOST_BLAST_DATABASE_PATH.value()));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile));

        int endOfDefline = fastaSequence.indexOf("\n");
        if (endOfDefline > 1) {
            bufferedWriter.write(fastaSequence.substring(0, endOfDefline) + "\n");
        }

        if (endOfDefline < 0) {
            endOfDefline = 0;
        }

        if (start >= 0 && finish > 0 && finish > start) {
            bufferedWriter.write(fastaSequence.substring(start + endOfDefline, finish + endOfDefline) + "\n");
        } else if (start >= 0) {
            bufferedWriter.write(fastaSequence.substring(start + endOfDefline) + "\n");
        } else if (finish >= 0) {
            bufferedWriter.write(fastaSequence.substring(0 + endOfDefline, finish + endOfDefline) + "\n");
        } else
            // nothing to substring
            if (start < 0 && finish < 0) {
                bufferedWriter.write(fastaSequence.substring(0 + endOfDefline) + "\n");
            } else {
                logger.warn("not sure how to handle this case start[" + start + "] finish[" + finish + "] length[" + fastaSequence.length() + "]");
                bufferedWriter.write(fastaSequence + "\n");
            }
        bufferedWriter.close();
        return tempFile;
    }

    protected Set<String> getAccessionsFromFile(File accessionFile) throws BlastDatabaseException {
        Set<String> accessions = new HashSet<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(accessionFile));

            String buffer;
            while ((buffer = bufferedReader.readLine()) != null) {
                accessions.add(buffer);
            }
            bufferedReader.close();
        } catch (IOException e) {
            logger.error(e);
            throw new BlastDatabaseException("failed to get accessions from file:" + accessionFile, e);
        }
        return accessions;
    }

    /**
     * Accessions are dumped into a temporary file, one on each line.
     *
     * @param validAccessions To dump from the database.
     * @param database        Database to dump from.
     * @return The file that the accessions were dumped into.
     * @throws IOException Failed to write accessions.
     */
    protected File createAccessionDump(Set<String> validAccessions, Database database) throws IOException {
        File tempFile = database.getTempFile("accessions", "txt");
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile));
        for (String accession : validAccessions) {
            bufferedWriter.write(accession + "\n");
        }
        bufferedWriter.close();
        return tempFile;
    }

    // ToDo: need to handle the exceptions better. Too clunky...
    public boolean validateCuratedDatabases() throws BlastDatabaseException {
        List<Database> databases = blastRepository.getDatabaseByOrigination(Origination.Type.CURATED);
        boolean success = true;
        for (Database database : databases) {
            logger.info("validating: " + database.getName());
            try {
                validateDatabase(database);
            } catch (BlastDatabaseException e) {
                logger.error(e.getMessage());
                success = false;
            }
        }
        if (!success)
            throw new BlastDatabaseException("Errors found during validation");
        return true;
    }


    public List<String> validateAllPhysicalDatabasesReadable() {
        List<Database> databases = blastRepository.getDatabaseByOrigination(Origination.Type.CURATED, Origination.Type.LOADED, Origination.Type.MARKERSEQUENCE);
        int numDatabases = getDatabaseStaticsCache().clearCache();
        logger.info("cleared cache of " + numDatabases + " databases");
        List<String> failures = new ArrayList<>();
        for (Database database : databases) {
            try {
                DatabaseStatistics databaseStatistics = getDatabaseStaticsCache().getDatabaseStatistics(database);
                if (databaseStatistics.getNumSequences() == 0) {
                    if (failures.size() == 0) {
                        logger.warn("0 sequences in database [" + database.getAbbrev() + "]: " + database);
                    } else {
                        logger.warn(failures.get(failures.size() - 1));
                    }

                } else if (databaseStatistics.getNumSequences() < 0) {
                    failures.add("no database available[" + database.getAbbrev() + "]: " + database);
                    logger.error(failures.get(failures.size() - 1));
                }
            } catch (BlastDatabaseException e) {
                logger.error(e);
                failures.add("error in database [" + database + "]: " + e);
            }
        }
        return failures;
    }

    /**
     * This method makes sure that there are at least as many valid entries in the blast database
     * as there are accessions.
     * Validates the database to make sure that it hasn't dropped sequences.
     *
     * @param database Database to validate.
     * @throws BlastDatabaseException Operation failure.
     */
    protected void validateDatabase(Database database) throws BlastDatabaseException {
        // do check 1
        int numAccessions = 0;
        Set<String> allValidAccessionNumbers = blastRepository.getAllValidAccessionNumbers(database);
        if (allValidAccessionNumbers != null)
            numAccessions = allValidAccessionNumbers.size();
        int numSequences = getDatabaseStatistics(database).getNumSequences();
        if (numAccessions > numSequences) {
            findMissingAccessions(database);
            throw new BlastDatabaseException("more accession/dblinks [" + numAccessions + "] than sequences [" + numSequences + "] in blast db[" + database.getAbbrev() + "]");
        }
        if (numAccessions != numSequences) {
            logger.warn(database.getDisplayName() + ": accessions [" + numAccessions + "] and sequences in Blast database [" + numSequences + "]");
        }
        try {
            File accessionFile = createAccessionDump(allValidAccessionNumbers, database);
            File remoteFile = getRemoteAccessionFile(accessionFile);
            checkAllAccessionsAreInDatabase(database, remoteFile);
        } catch (IOException e) {
            throw new BlastDatabaseException(": ");
        }


    }

    protected void checkAllAccessionsAreInDatabase(Database blastDatabase, File accessionFile) throws BlastDatabaseException {
        if (blastDatabase == null) {
            logger.error("failed to define primary blast database: ");
            return;
        }

        try {
            getLock(blastDatabase, false);
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
                defaultExecutor.execute(commandLine);
            } catch (IOException e) {
                logger.warn("problem with executing command: \n" + commandLine);
            }

            // parse output and see if there are errors.
            String errorOutput = "";
            for (String line : byteArrayErrorStream.toString().split("\n")) {
                if (
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
                throw new BlastDatabaseException("Failed to find all sequences in blast database [" + blastDatabase.getDisplayName() + "]: " + errorOutput);
            }

        } catch (BlastDatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.fatal(e);
            throw e;
        }
    }


    /**
     * This method makes sure that there are at least as many valid entries in the blast database
     * as there are accessions.
     *
     * @param database
     * @throws BlastDatabaseException
     */
    public void findMissingAccessions(Database database) throws BlastDatabaseException {

        try {
            // 1. get the accessions
            Set<String> validAccessions = blastRepository.getAllValidAccessionNumbers(database);
            Set<String> blastAccessions = new HashSet<>();
            // 2. dump accessions
            File accessionFileLocal = createAccessionDump(validAccessions, database);
            logger.debug("dumped accessions into: " + accessionFileLocal.getAbsoluteFile());

            // get remote file path
            File accessionFileRemote = getRemoteAccessionFile(accessionFileLocal);

            // 3. generate fasta with accessions
            File fastaFile = dumpDatabaseAsFastaForAccessions(database, accessionFileRemote);

            // 4. get the fasta file and figure out what the accessions are
            BufferedReader br = new BufferedReader(new FileReader(fastaFile));
            RichSequenceIterator sequenceIterator;

            if (database.getType() == Database.Type.NUCLEOTIDE) {
                sequenceIterator = RichSequence.IOTools.readFastaDNA(br, new SimpleNamespace(""));
            } else {
                sequenceIterator = RichSequence.IOTools.readFastaProtein(br, new SimpleNamespace(""));
            }
            while (sequenceIterator.hasNext()) {
                RichSequence richSequence = sequenceIterator.nextRichSequence();
                String accession = richSequence.getAccession();
                if (accession != null && accession.length() > 21) {
                    blastAccessions.add(accession.substring(4, 22));
                } else {
                    logger.error("bad accession in file: " + accession + " " + richSequence.getDescription() + " in file: " + fastaFile);
                }
                // do something with the sequence.
            }
            logger.info("# of blast accessions: " + blastAccessions.size());

            // 5. find what the missing accessions are
            Set<String> differences = new HashSet<>(CollectionUtils.disjunction(validAccessions, blastAccessions));
            File accessionDumpFile = createAccessionDump(differences, database);
            logger.error(differences.size() + " accessions missing from blast database written to: " + accessionDumpFile);
        } catch (BioException io) {
            throw new BlastDatabaseException("BioException database: " + database + " is invalid", io);
        } catch (IOException io) {
            throw new BlastDatabaseException("database: " + database + " is invalid", io);
        }
    }

    private File getRemoteAccessionFile(File local) {
        File remote = new File(ZfinPropertiesEnum.BLAST_ACCESSION_TEMP_DIR.value());
        return new File(remote, local.getName());
    }


    /**
     * Regenerates databases to make sure that they only include valid and the most accessions
     *
     * @param database Database to regenerate.
     * @throws BlastDatabaseException Operation failed.
     */
    protected void regenerateDatabaseFromValidAccessions(Database database) throws BlastDatabaseException {
        Set<String> validAccessions = blastRepository.getAllValidAccessionNumbers(database);
        List<String> previousAccession = blastRepository.getPreviousAccessionsForDatabase(database);
        if (CollectionUtils.isEqualCollection(validAccessions, previousAccession)) {
            logger.debug("collections have identical accessions for size " + validAccessions.size() + " for[" + database.getAbbrev() + "]");
            return;
        }
        logger.info("need to regenerate, # of valid accessions: " + validAccessions.size() + " for[" + database.getAbbrev() + "] vs previous: " + previousAccession.size());
        if (validAccessions.size() == 0) {
            logger.warn("No valid accessions dump from database[" + database.getName() + "]");
            return;
        }

        try {
            backupDatabase(database);
            File accessionFile = createAccessionDump(validAccessions, database);
            logger.info("dumped accessions into: " + accessionFile.getAbsoluteFile());
            File fastaFile = dumpDatabaseAsFastaForAccessions(database, accessionFile);
            logger.info("dumped fasta into: " + fastaFile.getAbsoluteFile());
            createDatabaseFromFasta(database, fastaFile);

            validateDatabase(database);
            updatePreviousAccessions(database, validAccessions, previousAccession);
        } catch (Exception e) {
            try {
                restoreDatabase(database);
            } catch (IOException e1) {
                throw new BlastDatabaseException("Failed to restore blast database: " + database, e1);
            }
            throw new BlastDatabaseException("Failed to regenerate blast database from valid accessions: " + database, e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void updatePreviousAccessions(Database database, Collection<String> validAccessions,
                                            Collection<String> previousAccession) {
        // nothing to update, just drop the old and add the new
        Transaction transaction = HibernateUtil.currentSession().getTransaction();
        transaction.begin();
        try {
            Collection<String> accessionToAdd = CollectionUtils.subtract(validAccessions, previousAccession);
            blastRepository.addPreviousAccessions(database, accessionToAdd);

            Collection<String> accessionToRemove = CollectionUtils.subtract(previousAccession, validAccessions);
            blastRepository.removePreviousAccessions(database, accessionToRemove);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        }
    }


    /**
     * Sets the lock when getting it
     *
     * @param database Database to lock.
     * @throws BlastDatabaseException Operation failed.
     */
    protected void getLock(Database database) throws BlastDatabaseException {
        getLock(database, true);
    }

    protected void getLock(Database database, boolean setLock) throws BlastDatabaseException {
        long currentTime = System.currentTimeMillis();
        try {
            while (database.isLocked()) {
                HibernateUtil.currentSession().refresh(database);
                Thread.sleep(200);
                if (System.currentTimeMillis() - currentTime > LOCK_TIMEOUT_MS) {
                    throw new BlastDatabaseException("Failed to get database lock, timeout: " + (System.currentTimeMillis() - currentTime));
                }
            }
        } catch (InterruptedException e) {
            logger.error("Failed to get database lock: " + e + "\n" + database.toString());
            throw new BlastDatabaseException("Failed to get database lock: " + e + "\n" + database.toString());
        }
        if (true == setLock) {
            database.setLocked(true);
        }
        HibernateUtil.currentSession().update(database);
        HibernateUtil.currentSession().flush();
    }


    /**
     * Releases the lock on this database.
     *
     * @param database the database to unlock.
     */
    protected void unlockForce(Database database) {
        database.setLocked(false);
        HibernateUtil.currentSession().update(database);
        HibernateUtil.currentSession().flush();
    }


    /**
     * Clips off the ending A's if the sequence ends with 6 A's.
     *
     * @param querySequence The sequence to clip
     * @return The clipped sequence string.
     */
    protected String clipPolyATail(String querySequence) {
        if (querySequence.toUpperCase().endsWith(POLYATAIL)) {
            querySequence = querySequence.toUpperCase();
            int lastIndex = querySequence.lastIndexOf(POLYATAIL);

            int scrubChar = lastIndex;
            char c = querySequence.charAt(scrubChar);
            for (
                    ; scrubChar > 0 && c == A_VALUE
                    ; c = querySequence.charAt(--scrubChar)) {

            }
            if (c == A_VALUE) {
                querySequence = querySequence.substring(0, scrubChar);
            } else {
                querySequence = querySequence.substring(0, scrubChar + 1);
            }
        }
        return querySequence;
    }


    public void setBlastResultFile(XMLBlastBean xmlBlastBean) throws IOException {
        File blastResultFile = xmlBlastBean.getResultFile();
        if (blastResultFile == null) {
            blastResultFile = File.createTempFile("blast", ".xml");
            logger.debug("setting blast result file: " + blastResultFile);
            xmlBlastBean.setResultFile(blastResultFile);
        }
    }


    /**
     * Fixes xml so its compliant with our dtd.
     *
     * @param returnXML    Blast result String.
     * @param xmlBlastBean The blast bean.
     * @return Returns the blast result String fixed.
     */
    protected String fixBlastXML(String returnXML, XMLBlastBean xmlBlastBean) {
        returnXML = returnXML.replaceFirst("<!DOCTYPE BlastOutput PUBLIC \"-//NCBI//NCBI BlastOutput/EN\" \"NCBI_BlastOutput.dtd\">", "");
        if (xmlBlastBean != null) {
            returnXML = returnXML.replaceFirst("<BlastOutput>", "<BlastOutput>"
                    + xmlBlastBean.getZFINParametersAsXML()
                    + "\n");
        } else {
            returnXML = returnXML.replaceFirst("<BlastOutput>", "<BlastOutput>"
                    + XMLBlastBean.getEmptyZFINParametersAsXML() +
                    "\n");
        }
        return returnXML;
    }

    /**
     * @param sequences Input sequences.
     * @return A unique list of sequences.
     */
    public List<Sequence> filterUniqueSequences(List<Sequence> sequences) {
        if (CollectionUtils.isEmpty(sequences)) {
            return sequences;
        }
        Iterator<Sequence> iter = sequences.iterator();
        Sequence sequence = iter.next();
        while (iter.hasNext()) {
            Sequence otherSequence = iter.next();
            if (otherSequence.getFormattedSequence().equals(sequence.getFormattedSequence())) {
                iter.remove();
            }
        }
        return sequences;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Sequence> getSequencesForAccessionAndReferenceDBs(String accession, ReferenceDatabase... referenceDatabases) throws BlastDatabaseException {
        return getSequencesForAccessionAndReferenceDBs(RepositoryFactory.getSequenceRepository().getDBLinks(accession, referenceDatabases));
    }

    public List<Sequence> getSequencesForAccessionAndReferenceDBs(List<DBLink> dbLinks) throws BlastDatabaseException {
        List<Sequence> sequences = new ArrayList<>();
        try {
            for (DBLink dbLink : dbLinks) {
                // if there is a primary blast database, then all is good
                if (dbLink.getReferenceDatabase().getPrimaryBlastDatabase() != null) {
                    sequences.addAll(getSequencesFromSource(dbLink));
                }
            }
        } catch (Exception e) {
            logger.warn("Unable to get sequences from dblinks", e);
            throw new BlastDatabaseException("Failed to get Sequences for accession and reference DB, remote or local", e);
        }
        return sequences;
    }

    public String robustlyBlastOneDBToString(XMLBlastBean xmlBlastBean) throws BlastDatabaseException, BusException {
        String xml = null;
        for (int attempts = 0; xml == null && attempts < maxAttempts; ++attempts) {
            try {
                xml = blastOneDBToString(xmlBlastBean);
            } catch (BusException e) {
                if (attempts == maxAttempts - 1) {
//                    throw new BlastDatabaseException("too many bus errors: "+maxAttempts + " for ticket: "+ xmlBlastBean.getTicketNumber()+"\n"+xmlBlastBean) ;
                    throw new BusException("too many bus errors: " + maxAttempts + " for ticket: " + xmlBlastBean.getTicketNumber() + "\n" + xmlBlastBean, e.getReturnXML());
                } else {
                    logger.warn("Bus exception for ticket, re-submitting ticket: " + xmlBlastBean.getTicketNumber(), e);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        throw new BlastDatabaseException("sleeping thread interrupted for ticket: " +
                                xmlBlastBean.getTicketNumber() + "\n" + xmlBlastBean, ie);
                    }
                }
            }
        }
        return xml;
    }

    public String robustlyBlastOneDBToString(XMLBlastBean xmlBlastBean, Database database) throws BlastDatabaseException, BusException {
        String xml = null;
        for (int attempts = 0; xml == null && attempts < maxAttempts; ++attempts) {
            try {
                xml = blastOneDBToString(xmlBlastBean, database);
            } catch (BusException e) {
                if (attempts == maxAttempts - 1) {
//                    throw new BlastDatabaseException("too many bus errors: "+maxAttempts + " for ticket: "+ xmlBlastBean.getTicketNumber()+"\n"+xmlBlastBean) ;
                    throw new BusException("too many bus errors: " + maxAttempts + " for ticket: " + xmlBlastBean.getTicketNumber() + "\n" + xmlBlastBean, e.getReturnXML());
                } else {
                    logger.warn("Bus exception for ticket, re-submitting ticket: " + xmlBlastBean.getTicketNumber(), e);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        throw new BlastDatabaseException("sleeping thread interrupted for ticket: " +
                                xmlBlastBean.getTicketNumber() + "\n" + xmlBlastBean, ie);
                    }
                }
            }
        }
        return xml;
    }

    public List<Sequence> getSequencesForMarker(Marker marker, ReferenceDatabase... referenceDatabases) throws BlastDatabaseException {
        List<MarkerDBLink> markerDBLinks = RepositoryFactory.getSequenceRepository().getDBLinksForMarker(marker, referenceDatabases);
        List<Sequence> sequences = new ArrayList<>();
        for (MarkerDBLink dbLink : markerDBLinks) {
            sequences.addAll(getSequencesFromSource(dbLink));
        }
        return sequences;
    }

    /**
     * @param transcript Transcript to get sequences from
     * @param groupName  Display group that sequences must get belong to via their reference database
     * @return List of sequences associated with the transcript and the group name.
     */
    public List<Sequence> getSequencesForTranscript(Transcript transcript, DisplayGroup.GroupName groupName) throws BlastDatabaseException {
        Set<TranscriptDBLink> links = transcript.getTranscriptDBLinks();
        List<Sequence> sequences = new ArrayList<>();
        for (TranscriptDBLink transcriptDBLink : links) {
            if (transcriptDBLink.getReferenceDatabase().isInDisplayGroup(groupName)) {
                List<Sequence> sequenceList = getSequencesFromSource(transcriptDBLink);
                logger.debug("refdb CONTAINED # of sequences: " + (sequenceList == null ? "null" : sequenceList.size()));
                if (CollectionUtils.isNotEmpty(sequenceList)) {
                    for (Sequence sequence : sequenceList) {
                        if (sequence.getDbLink().getZdbID().equals(transcriptDBLink.getZdbID())) {
                            sequences.add(sequence);
                        }
                    }
                }
            } else {
                logger.debug("refdb not contained: " + transcriptDBLink.getReferenceDatabase().getForeignDB().getDbName());
            }
        }
        logger.debug("Transcript!!read only sequences: " + sequences.size());

        Collections.sort(sequences);
        return sequences;
    }


    /**
     * todo: will this ever get used?
     *
     * @param dbLink
     * @return A list of sequences
     */
    protected List<Sequence> getSequencesFromSource(DBLink dbLink) {
        Database blastDatabase = dbLink.getReferenceDatabase().getPrimaryBlastDatabase();
        String accession = dbLink.getAccessionNumber();
        if (blastDatabase == null) {
            logger.error("No primary blast database defined for accession: " + accession);
            return new ArrayList<>();
        }
        List<String> commandList = new ArrayList<>();

        commandList.addAll(getPrefixCommands());
        commandList.add(getKeyPath() + getBlastGetBinary());
        commandList.add("-" + blastDatabase.getTypeCharacter());
        commandList.add(getCurrentDatabasePath(blastDatabase));
        commandList.add(accession);

        FastaReadProcess execProcess = new FastaReadProcess(commandList, dbLink);
        logger.info("getSequencesFromSource exec string: " + execProcess);

        try {
            getLock(blastDatabase, false);

            execProcess.exec();

            if (execProcess.getStandardError().toString().length() > 0) {
                logger.fatal("Failed to xdget, stderr: " + System.lineSeparator() + execProcess.getStandardError());
                logger.fatal("Failed to xdget, stdout: " + System.lineSeparator() + execProcess.getStandardOutput());
            }
            return execProcess.getSequences();
        } catch (Exception e) {
            logger.error("Failed to xdget, stderr: " + System.lineSeparator() + execProcess.getStandardError());
            logger.error("Failed to xdget, stdout: " + System.lineSeparator() + execProcess.getStandardOutput());
            logger.error("Failed to retrieve sequences with command [" + commandList.toString().replaceAll(",", " ") + "\n", e);
            return new ArrayList<>();
        } finally {
            unlockForce(blastDatabase);
        }
    }

    /**
     * Get from a remote service.
     *
     * @param blastDatabase Blast database to retrieve sequences from.
     * @return The number of sequences.
     * @throws BlastDatabaseException Thrown if it fails to get to the database.
     */
    public DatabaseStatistics getDatabaseStatistics(Database blastDatabase) throws BlastDatabaseException {
        DatabaseStatistics databaseStatistics = new DatabaseStatistics();
        int numOfSequences = -1;
        if (blastDatabase == null) {
            logger.error("failed to define primary blast database: " + blastDatabase);
            return databaseStatistics;
        }


        List<String> commandList = new ArrayList<>();
        commandList.addAll(getPrefixCommands());
        commandList.add(getBlastPutBinary());
        commandList.add("-" + (blastDatabase.getTypeCharacter()));
        commandList.add("-i");
        commandList.add(getCurrentDatabasePath(blastDatabase));

        ExecProcess execProcess = new ExecProcess(commandList);

        try {


            logger.info("NumberOfSequences exec string: " + execProcess);

            execProcess.exec();


            // dump output
//            while ((line = stderr.readLine()) != null ) {

            String[] lines = execProcess.getStandardError().split("\n");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:MM:SS a z MMM d, yyyy");

            for (int i = 0; i < lines.length && false == databaseStatistics.isSet(); i++) {
                String line = lines[i];
//                for (String line : execProcess.getStandardError().split("\n") ) {
                if (line.contains("No. of sequences")) {
                    line = line.replace(",", "");
                    int index1 = line.indexOf(":");
                    int index2 = line.indexOf("(", index1 + 1);
                    // filter out ","
                    String sequenceNumberString = line.substring(index1 + 1, index2).trim();
                    logger.debug("seq num str: " + sequenceNumberString);
                    numOfSequences = Integer.parseInt(sequenceNumberString);
                    databaseStatistics.setNumSequences(numOfSequences);
                } else if (line.contains("Creation date:")) {
                    Date creationDate = simpleDateFormat.parse(line.substring(15).trim());
                    databaseStatistics.setCreationDate(creationDate);
                } else if (line.contains("Modified date:")) {
                    Date modifiedDate = simpleDateFormat.parse(line.substring(15).trim());
                    databaseStatistics.setModifiedDate(modifiedDate);
                }
            }


            logger.debug("error std output: " + execProcess.getStandardError());
            logger.debug("command std output: " + execProcess.getStandardOutput());
        } catch (Exception e) {
            logger.error(e);
            logger.error("stderr: " + System.lineSeparator() + execProcess.getStandardError());
            logger.error("stdout: " + System.lineSeparator() + execProcess.getStandardOutput());
            throw new BlastDatabaseException("Failed to retrieve information for database[" + blastDatabase + "]", e);
        }
        return databaseStatistics;
    }


    public String removeLeadingNumbers(String fileData, XMLBlastBean.SequenceType sequenceType) {
        String[] lines = fileData.split("[\n]");
        logger.debug("# lines: " + lines.length);
        if (lines.length == 1) {
            if (sequenceType == XMLBlastBean.SequenceType.NUCLEOTIDE) {
                lines = fixDeflineNucleotideSequence(lines[0]);
            } else {
                lines = fixDeflineProteinSequence(lines[0]);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            line = line.trim();
            if (line.indexOf(">") < 0) {
                line = line.replaceAll("[0-9, ]", "").trim();
            }
            sb.append(line).append("\n");
        }
        logger.debug("input:\n[" + fileData + "]");
        logger.debug("output:\n[" + sb.toString() + "]");
        return sb.toString();
    }

    /**
     * If the sequence is only a single line (no return after the defline), it looks for the sequence and
     * automatically breaks the line.
     *
     * @param line Un-formatted sequence.
     * @return The first string is the defline, the second string is the sequence.
     */
    public String[] fixDeflineNucleotideSequence(String line) {
        Matcher m = nucleotideSequencePattern.matcher(line);
        if (m.find()) {
            String[] returnStrings = new String[2];
            returnStrings[0] = line.substring(0, m.start());
            returnStrings[1] = line.substring(m.start());
            return returnStrings;
        }
        String[] returnString = new String[1];
        returnString[0] = line;
        return returnString;
    }

    /**
     * If the sequence is only a single line (no return after the defline), it looks for the sequence and
     * automatically breaks the line.
     *
     * @param line Un-formatted sequence.
     * @return The first string is the defline, the second string is the sequence.
     */
    public String[] fixDeflineProteinSequence(String line) {
        Matcher m = proteinSequencePattern.matcher(line);
        if (m.find()) {
            String[] returnStrings = new String[2];
            returnStrings[0] = line.substring(0, m.start());
            returnStrings[1] = line.substring(m.start());
            return returnStrings;
        }
        String[] returnString = new String[1];
        returnString[0] = line;
        return returnString;
    }
}
