package org.zfin.gwt.curation.ui;

/**
 * Each module that is served by the CurationEntryPoint has a corresponding
 * enumeration entry here.
 *
 */
public enum CurationModuleType {

        FX_CURATION{
            @Override
            public PileConstructionZoneModule initializeModule(String publicationID){
                FxCurationModule module = new FxCurationModule(publicationID);
                return module.getPileConstructionZoneModule();
            }
        },
        PHENOTYPE_CURATION{
            @Override
            public PileConstructionZoneModule initializeModule(String publicationID){
                PhenotypeCurationModule module = new PhenotypeCurationModule(publicationID);
                return module.getPileConstructionZoneModule();
            }
        };

        public static CurationModuleType getType(String type) {
            for (CurationModuleType t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No module type of string " + type + " found.");
        }

        public abstract PileConstructionZoneModule initializeModule(String publicationID);

}
