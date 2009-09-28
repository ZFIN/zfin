package org.zfin.sequence;

import org.zfin.orthology.Species;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.repository.RepositoryFactory;

/**
 */
public class SequenceService {

    private final static SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository() ;


    public static ReferenceDatabase getOMIMHumanOrthologue() {
        return sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.OMIM,
                ForeignDBDataType.DataType.ORTHOLOGUE,
                ForeignDBDataType.SuperType.ORTHOLOGUE,
                Species.HUMAN);
    }

    public static ReferenceDatabase getEntrezGeneHumanRefDB() {
        return sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.ENTREZ_GENE,
                ForeignDBDataType.DataType.ORTHOLOGUE,
                ForeignDBDataType.SuperType.ORTHOLOGUE,
                Species.HUMAN);
    }

    public static ReferenceDatabase getEntrezGeneMouseRefDB() {
        return sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.ENTREZ_GENE,
                ForeignDBDataType.DataType.ORTHOLOGUE,
                ForeignDBDataType.SuperType.ORTHOLOGUE,
                Species.MOUSE);
    }
}
