package org.zfin.marker.agr;

import org.zfin.expression.ExperimentCondition;
import org.zfin.ontology.GenericTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

/**
 * Maps an {@link ExperimentCondition}'s ZECO term onto the high-level
 * conditionClassId the Alliance expects, setting the finer-grained term as
 * conditionId when the two differ.
 *
 * <p>Extracted from {@code DiseaseInfo} when ZFIN-10383 retired the DAF
 * generator: {@code HibernateMutantRepository} calls this on the phenotype
 * path, which has nothing to do with generating the DAF file, so the logic
 * outlived the class that happened to host it.
 */
public final class ExperimentConditionClassifier {

    private ExperimentConditionClassifier() {}

    // ToDo: This list should be a slim in ZECO to identify those high-level terms.
    private static final List<GenericTerm> highLevelConditionTerms = new ArrayList<>(18);

    static {
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-7", "ZECO:0000105"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-13", "ZECO:0000111"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-14", "ZECO:0000112"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-15", "ZECO:0000113"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-33", "ZECO:0000131"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-42", "ZECO:0000140"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-45", "ZECO:0000143"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-48", "ZECO:0000146"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-56", "ZECO:0000154"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-62", "ZECO:0000160"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-82", "ZECO:0000182"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-108", "ZECO:0000208"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-122", "ZECO:0000222"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-129", "ZECO:0000229"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-171108-6", "ZECO:0000252"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-3", "ZECO:0000101"));
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-5", "ZECO:0000103"));
        // make sure it's the last entry as it is a root term.
        highLevelConditionTerms.add(new GenericTerm("ZDB-TERM-160831-6", "ZECO:0000104"));
    }

    public static void populateConditionClassId(ExperimentConditionDTO expcond, ExperimentCondition condition) {
        String oboID = condition.getZecoTerm().getOboID();
        if (highLevelConditionTerms.stream().map(GenericTerm::getOboID).toList().contains(oboID)) {
            expcond.setConditionClassId(oboID);
        } else {
            Optional<GenericTerm> highLevelterm = highLevelConditionTerms.stream().filter(parentTerm -> getOntologyRepository().isParentChildRelationshipExist(parentTerm, condition.getZecoTerm()))
                    .findFirst();
            if (highLevelterm.isPresent()) {
                expcond.setConditionClassId(highLevelterm.get().getOboID());
                expcond.setConditionId(oboID);
            }
        }
    }
}
