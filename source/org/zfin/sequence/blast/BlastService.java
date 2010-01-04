package org.zfin.sequence.blast;

import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.blast.presentation.XMLBlastBean;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Service class for accesing the blast databases.
 */
public interface BlastService {

    static final String CURRENT_DIRECTORY = "Current";
    static final String BACKUP_DIRECTORY = "Backup";

    // filter constants
    static final String POLYATAIL = "AAAAAA";
    static final char A_VALUE = 'A';
    static final String FILTER_SEG = "seg";
    static final String FILTER_XNU = "xnu";
    static final String FILTER_DUST = "dust";

    // timeout constant
    static final int LOCK_TIMEOUT_MS = 3000;

    Sequence addSequenceToTranscript(String transcriptZdbID, String sequenceData, String referenceDatabaseZdbID) throws BlastDatabaseException;

    Sequence addSequenceToMarker(Marker marker, String sequenceData, String pubZdbID, ReferenceDatabase referenceDatabase) throws BlastDatabaseException;

    Sequence addProteinToMarker(Marker marker, String sequenceData, String pubZdbID, ReferenceDatabase referenceDatabase) throws BlastDatabaseException;

    List<Sequence> getSequencesForTranscript(Transcript transcript, DisplayGroup.GroupName groupName) throws BlastDatabaseException;

    List<Sequence> getSequencesForMarker(Marker marker, ReferenceDatabase... referenceDatabases) throws BlastDatabaseException;

    List<Sequence> getSequencesForAccessionAndDisplayGroup(String accession, DisplayGroup.GroupName... groupNames) throws BlastDatabaseException;

    List<Sequence> getSequencesForAccessionAndReferenceDBs(String accession, ReferenceDatabase... referenceDatabases) throws BlastDatabaseException;

    List<Sequence> getSequencesForAccessionAndReferenceDBs(List<DBLink> dbLinks) throws BlastDatabaseException;


    /**
     * This method wraps blastOneDBToStream for the case of a single database that is not split.
     * @param xmlBlastBean Blast parameters beans.
     * @return An output stream of an ExecuteBlastBean with populated results.
     * @throws BlastDatabaseException Operation failure.
     */
//    BlastOutput blastOneDB(XMLBlastBean xmlBlastBean) throws BlastDatabaseException;

    /**
     * This method wraps blastOneDBToString with multiple attempts.
     *
     * @param xmlBlastBean Blast parameters beans.
     * @param database     Database to blast.
     * @return An output stream of an ExecuteBlastBean with populated results.
     * @throws BlastDatabaseException Operation failure.
     */
    String robustlyBlastOneDBToString(XMLBlastBean xmlBlastBean, Database database) throws BlastDatabaseException, BusException;

    /**
     * This method performs a remote blast using ssh and returns the output stream
     *
     * @param xmlBlastBean Blast parameters beans.
     * @param database     Database to blast.
     * @return An output stream of an ExecuteBlastBean with populated results.
     * @throws BlastDatabaseException Operation failure.
     * @throws BusException           Bus error has been thrown.
     */
    String blastOneDBToString(XMLBlastBean xmlBlastBean, Database database) throws BlastDatabaseException, BusException;

    /**
     * This method returns the number of sequences in a blast database.
     *
     * @param database Database to get sequences from.
     * @return Number of sequences in blast database.
     * @throws IOException Failed to get the sequences.
     */
    DatabaseStatistics getDatabaseStatistics(Database database) throws BlastDatabaseException;

    /**
     * Regenerates databases to make sure that they only include valid and the most accessions
     *
     * @throws BlastDatabaseException Operation failed.
     */
    void regenerateCuratedDatabases() throws BlastDatabaseException;

    /**
     * @param database Database to backup
     * @return Output files backed up.
     * @throws IOException Throws in fails to copy
     */
    List<File> backupDatabase(Database database) throws IOException;


    /**
     * @param database Database to restore
     * @return Files restored.
     * @throws IOException Throws in fails to copy
     */
    List<File> restoreDatabase(Database database) throws IOException;

    /**
     * Validates all loaded and curated databases to make sure that the number of accessions matches.
     *
     * @throws BlastDatabaseException Operation failure.
     */
    boolean validateCuratedDatabases() throws BlastDatabaseException;

    /**
     * Validates all physically readable databases (ie, non-generated, non-external) to make sure that
     * there is at least one sequence available for blasting or reading.
     *
     * @return A list of failed databases as String messages.
     * @throws BlastDatabaseException
     */
    List<String> validateAllPhysicalDatabasesReadable() throws BlastDatabaseException;

    /**
     * Sets a temporary blast file on an XML BlastBean
     *
     * @param xmlBlastBean The bean to set the blastResultFile for.
     * @throws IOException Operation failed.
     */
    public void setBlastResultFile(XMLBlastBean xmlBlastBean) throws IOException;

    /**
     * This class removes sequences that are identical, even if they have different links
     *
     * @param sequences Input sequences.
     * @return Sequences with unique sequence data
     */
    public List<Sequence> filterUniqueSequences(List<Sequence> sequences);

    /**
     * removes numbers from sequence, but not from defline
     *
     * @param fileData Sequence to filter.
     * @return Filtered sequence.
     */
    public String removeLeadingNumbers(String fileData,XMLBlastBean.SequenceType sequenceType) ;
}
