package org.zfin.sequence.service;

import org.zfin.orthology.Species;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.repository.SequenceRepository;

/**
 */
//@Service
public class SequenceService {

    private final static SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();

    private ReferenceDatabase omimHumanOrthologueDB;
    private ReferenceDatabase entrezGeneHumarnRefDB;
    private ReferenceDatabase entrezGeneMouseRefDB;
    private ReferenceDatabase uniprotDB;

    public ReferenceDatabase getOMIMHumanOrthologue() {
        if (omimHumanOrthologueDB == null) {
            omimHumanOrthologueDB = sequenceRepository.getReferenceDatabase(
                    ForeignDB.AvailableName.OMIM,
                    ForeignDBDataType.DataType.ORTHOLOGUE,
                    ForeignDBDataType.SuperType.ORTHOLOGUE,
                    Species.HUMAN);
        }
        return omimHumanOrthologueDB;
    }

    public ReferenceDatabase getEntrezGeneHumanRefDB() {
        if (entrezGeneHumarnRefDB == null) {
            entrezGeneHumarnRefDB = sequenceRepository.getReferenceDatabase(
                    ForeignDB.AvailableName.ENTREZ_GENE,
                    ForeignDBDataType.DataType.ORTHOLOGUE,
                    ForeignDBDataType.SuperType.ORTHOLOGUE,
                    Species.HUMAN);
        }

        return entrezGeneHumarnRefDB;
    }

    public ReferenceDatabase getEntrezGeneMouseRefDB() {
        if (entrezGeneMouseRefDB == null) {
            entrezGeneMouseRefDB = sequenceRepository.getReferenceDatabase(
                    ForeignDB.AvailableName.ENTREZ_GENE,
                    ForeignDBDataType.DataType.ORTHOLOGUE,
                    ForeignDBDataType.SuperType.ORTHOLOGUE,
                    Species.MOUSE);
        }

        return entrezGeneMouseRefDB;
    }

    public ReferenceDatabase getUniprotDb() {
        if (uniprotDB == null) {
            uniprotDB = sequenceRepository.getReferenceDatabase(
                    ForeignDB.AvailableName.UNIPROTKB,
                    ForeignDBDataType.DataType.POLYPEPTIDE,
                    ForeignDBDataType.SuperType.SEQUENCE,
                    Species.ZEBRAFISH);
        }

        return uniprotDB;
    }

    public static ReferenceDatabase getUniprotRefDB() {
        return sequenceRepository.getZebrafishSequenceReferenceDatabase(
                ForeignDB.AvailableName.UNIPROTKB
                , ForeignDBDataType.DataType.POLYPEPTIDE
        );
    }

}
