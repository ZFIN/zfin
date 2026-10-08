package org.zfin.sequence;

/**
 * Projection returned by SequenceRepository.getForeignDbUrlCheckCandidates() —
 * one foreign_db row paired with a single real accession drawn from
 * foreign_db_contains/db_link, so CheckForeignDbUrlsTask can build a live
 * example URL (dbUrlPrefix + accession + dbUrlSuffix) and curl it. A
 * foreign_db row with no accession anywhere in db_link (nothing ever used
 * it) comes back with a null exampleAccession and is skipped by the task.
 */
public record ForeignDbUrlCheckRow(
        Long foreignDbId,
        String dbName,
        String dbUrlPrefix,
        String dbUrlSuffix,
        String exampleAccession
) {
}
