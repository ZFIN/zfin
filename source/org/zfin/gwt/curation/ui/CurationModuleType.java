package org.zfin.gwt.curation.ui;

/**
 * Each module that is served by the CurationEntryPoint has a corresponding
 * enumeration entry here.
 */
public enum CurationModuleType {

    FX_CURATION {
        @Override
        public PileConstructionZoneModule initializeModule(String publicationID) {
            FxCurationModule module = new FxCurationModule(publicationID);
            return module.getPileConstructionZoneModule();
        }
    },
    PHENOTYPE_CURATION {
        @Override
        public PileConstructionZoneModule initializeModule(String publicationID) {
            PhenotypeCurationModule module = new PhenotypeCurationModule(publicationID);
            return module.getPileConstructionZoneModule();
        }
    },
    GO_CURATION {
        @Override
        public ConstructionZone initializeModule(String publicationID) {
            GoCurationModule module = new GoCurationModule(publicationID);
            return module.getPileConstructionZoneModule();
        }
    },
    ENVIRONMENT_CURATION {
        @Override
        public ConstructionZone initializeModule(String publicationID) {
            BaseCurationModule module = new BaseCurationModule(publicationID);
            return module.getPileConstructionZoneModule();
        }
    },
    FIGURE_CURATION {
        @Override
        public ConstructionZone initializeModule(String publicationID) {
            BaseCurationModule module = new BaseCurationModule(publicationID);
            return module.getPileConstructionZoneModule();
        }
    },
    GENOTYPE_CURATION {
        @Override
        public ConstructionZone initializeModule(String publicationID) {
            BaseCurationModule module = new BaseCurationModule(publicationID);
            return module.getPileConstructionZoneModule();
        }
    },
    FEATURE_CURATION {
        @Override
        public ConstructionZone initializeModule(String publicationID) {
            BaseCurationModule module = new BaseCurationModule(publicationID);
            return module.getPileConstructionZoneModule();
        }
    },
    CONSTRUCT_CURATION {
        @Override
        public ConstructionZone initializeModule(String publicationID) {
            BaseCurationModule module = new BaseCurationModule(publicationID);
            return module.getPileConstructionZoneModule();
        }
    },;

    public static CurationModuleType getType(String type) {
        for (CurationModuleType t : values()) {
            if (t.toString().equals(type))
                return t;
        }
        throw new RuntimeException("No module type of string " + type + " found.");
    }

    public abstract ConstructionZone initializeModule(String publicationID);

}
