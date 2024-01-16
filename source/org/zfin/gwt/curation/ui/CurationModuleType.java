package org.zfin.gwt.curation.ui;

import org.zfin.framework.featureflag.FeatureFlagEnum;
import org.zfin.framework.featureflag.FeatureFlags;
import org.zfin.gwt.curation.ui.disease.HumanDiseaseModule;
import org.zfin.gwt.curation.ui.experiment.ExperimentModule;
import org.zfin.gwt.curation.ui.feature.FeatureModule;
import org.zfin.gwt.curation.ui.fish.FishModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Each module that is served by the CurationEntryPoint has a corresponding
 * enumeration entry here.
 */
public enum CurationModuleType {

    CONSTRUCT("CONSTRUCT", "Construct") {
        @Override
        public ZfinCurationModule initializeModule(String publicationID) {
            return new ConstructCurationModule(publicationID);
        }
    },
    CONSTREACT("CONSTREACT", "Construct v2") { //eventually rename to CONSTRUCT when replacing the original version
        @Override
        public ZfinCurationModule initializeModule(String publicationID) {
            return null;
        }
    },
    FEATURE_CURATION("FEATURE", "Feature") {
        @Override
        public ZfinCurationModule initializeModule(String publicationID) {
            return new FeatureModule(publicationID);
        }
    },
    FISH_TAB("FISH", "Fish") {
        @Override
        public ZfinCurationModule initializeModule(String publicationID) {
            return new FishModule(publicationID);
        }
    },
    EXPERIMENT_TAB("EXPERIMENT", "Experiment") {
        @Override
        public ZfinCurationModule initializeModule(String publicationID) {
            return new ExperimentModule(publicationID);
        }
    },
    EXPRESSION_CURATION("FX", "FX") {
        @Override
        public ZfinCurationModule initializeModule(String publicationID) {
            return new ExpressionModule(publicationID);
        }
    },
    PHENOTYPE_CURATION("PHENO", "PHENO") {
        @Override
        public ZfinCurationModule initializeModule(String publicationID) {
            return new PhenotypeCurationModule(publicationID);
        }
    },
    DISEASE_CURATION("DISEASE", "Disease") {
        @Override
        public ZfinCurationModule initializeModule(String publicationID) {
            return new HumanDiseaseModule(publicationID);
        }
    },
    ORTHOLOGY("ORTHOLOGY", "Orthology") {
        @Override
        public ZfinCurationModule initializeModule(String publicationID) {
            return null;
        }
    };

    public static CurationModuleType getType(String type) {
        for (CurationModuleType t : values()) {
            if (t.value.equals(type))
                return t;
        }
        throw new RuntimeException("No module type of string " + type + " found.");
    }

    private final String value;
    private final String displayName;

    CurationModuleType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Return all curation tabs. If the CONSTRUCT REACT tab is enabled, it will be returned, otherwise skipped.
     * @return list of curation tabs
     */
    public static List<CurationModuleType> enabledCurationTabs() {
        if (FeatureFlags.isFlagEnabled(FeatureFlagEnum.USE_REACT_CONSTRUCT_TAB)) {
            return Arrays.asList(values());
        } else {
            return Arrays.stream(values()).filter(t -> t != CONSTREACT).collect(Collectors.toList());
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract ZfinCurationModule initializeModule(String publicationID);

    public static List<CurationModuleType> getOtherTypes(CurationModuleType type) {
        if (type == null) {
            return Arrays.asList(values());
        }
        List<CurationModuleType> list = new ArrayList<>();
        for (CurationModuleType typeOther : values()) {
            if (typeOther != type)
                list.add(typeOther);
        }
        return list;
    }

    public String getValue() {
        return value;
    }
}
