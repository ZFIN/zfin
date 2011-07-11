package org.zfin.sequence.blast;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.Sequence;

import java.util.ArrayList;
import java.util.List;

/**
 * A convenience service class that accesses both remote and local services.
 */
public class MultipleBlastServerService {

    private final static Logger logger = Logger.getLogger(MultipleBlastServerService.class);

    public static List<Sequence> getSequencesForAccessionAndReferenceDBs(String accession, ReferenceDatabase... referenceDatabases) {
        return getSequencesForAccessionAndReferenceDBs(accession,true,referenceDatabases) ;
    }

    /**
     * You have to have dblinks in order to retrieve sequence.
     *
     * @param accession          Accession to query.
     * @param allowRemoteFetch
     *@param referenceDatabases ReferenceDatabases to search for dblinks in.  @return A list of sequences.
     */
    public static List<Sequence> getSequencesForAccessionAndReferenceDBs(String accession, boolean allowRemoteFetch, ReferenceDatabase... referenceDatabases) {

        List<Sequence> sequences = new ArrayList<Sequence>();
        accession = accession.toUpperCase();
        List<DBLink> dbLinks = RepositoryFactory.getSequenceRepository().getDBLinks(accession, referenceDatabases);
        if (CollectionUtils.isNotEmpty(dbLinks)) {
            List<DBLink> internallyRetievableSequences = getInternallyRetrievableSequences(dbLinks);
            if (CollectionUtils.isNotEmpty(internallyRetievableSequences)) {
                DatabaseStatistics databaseStatistics;
                try {
                    databaseStatistics = WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(internallyRetievableSequences.get(0).getReferenceDatabase().getPrimaryBlastDatabase());
                    if (databaseStatistics.getNumSequences() > 0) {
                        sequences = MountedWublastBlastService.getInstance().getSequencesForAccessionAndReferenceDBs(dbLinks);
                    }
                } catch (BlastDatabaseException e) {
                    logger.error("Failed to retrieve sequence locally [" + accession + "]", e);
                }
            }

            if (CollectionUtils.isEmpty(sequences) && allowRemoteFetch) {
                try {
                    sequences.addAll(getSequenceFromNCBI(dbLinks));
                } catch (BlastDatabaseException e) {
                    logger.error("Failed to retrieve sequence from NCBI [" + accession + "]", e);
                }
            }
        } else {
            // assume that the first referenceDatabase indicates the sequence type, if it exists
            List<Sequence> fastaSequences = NCBIEfetch.getSequenceForAccession(accession);
            sequences.addAll(fastaSequences);
        }
        return sequences;
    }

    /**
     * @param dbLinks List of dblinks to check.
     * @return Returns true if at least one DBLink has a valid primary blast database.
     */
    public static List<DBLink> getInternallyRetrievableSequences(List<DBLink> dbLinks) {
        List<DBLink> returnSeqDbLinks = new ArrayList<DBLink>();
        for (DBLink dbLink : dbLinks) {
            if (true == dbLink.isInternallyRetievableSequence()) {
                returnSeqDbLinks.add(dbLink);
            }
        }
        return returnSeqDbLinks;
    }

    public static List<Sequence> getSequenceFromNCBI(List<DBLink> dbLinks) throws BlastDatabaseException {
        List<Sequence> returnSequences = new ArrayList<Sequence>();
        if (CollectionUtils.isEmpty(dbLinks)) {
            return returnSequences;
        }

        String accession = dbLinks.get(0).getAccessionNumber();
        List<Sequence> fastaSequences = NCBIEfetch.getSequenceForAccession(accession);

        try {
            for (DBLink dbLink : dbLinks) {
                // set the dblinks for the sequences
                for (Sequence sequence : fastaSequences) {
                    sequence.setDbLink(dbLink);
                }
                returnSequences.addAll(fastaSequences);
            }
        } catch (Exception e) {
            logger.warn("Problem getting NCBI sequence from dblinks", e);
            throw new BlastDatabaseException("Failed to get Sequences for accession and reference DB, remote or local", e);
        }
        return returnSequences;
    }
}
