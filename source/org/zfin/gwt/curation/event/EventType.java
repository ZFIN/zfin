package org.zfin.gwt.curation.event;

public enum EventType {
    // @formatter:off
        ROOT(),
            ADD_REMOVE_ATTRIBUTION(ROOT),
                ATTRIBUTE(ADD_REMOVE_ATTRIBUTION),
                DEATTRIBUTE(ADD_REMOVE_ATTRIBUTION),
            FILTER(ROOT),
                FILTER_ENTITY(FILTER),
                UNSELECT_ENTITY(FILTER),
                    UNSELECT_FX(UNSELECT_ENTITY),
                    UNSELECT_PHENO(UNSELECT_ENTITY),
            CONSTRUCT_TAB(ROOT),
            FEATURE_TAB(ROOT),
            FISH_TAB(ROOT),
            EXPERIMENT_TAB(ROOT),
            FX_TAB(ROOT),
                FILTER_FX(FILTER_ENTITY, FX_TAB),
                CREATE_EXPRESSION_EXPERIMENT(FX_TAB),
            PHENO_TAB(ROOT),
                FILTER_PHENO(FILTER_ENTITY, PHENO_TAB),
            GO_TAB(ROOT),
            DISEASE_TAB(ROOT),
                CREATE_DISEASE_MODEL(DISEASE_TAB),
            FX(ROOT),
            FEATURE(ROOT),
                CUD_FEATURE(FEATURE),
                    CREATE_FEATURE(CUD_FEATURE),
                    EDIT_FEATURE(CUD_FEATURE),
                CUD_FEATURE_RELATIONSHIP(FEATURE),
                    CREATE_FEATURE_RELATIONSHIP(CUD_FEATURE_RELATIONSHIP),
                    DELETE_FEATURE_RELATIONSHIP(CUD_FEATURE_RELATIONSHIP),
                ADD_REMOVE_ATTRIBUTION_FEATURE(FEATURE, ADD_REMOVE_ATTRIBUTION),
                    ATTRIBUTE_FEATURE(ADD_REMOVE_ATTRIBUTION_FEATURE),
                    DEATTRIBUTE_FEATURE(ADD_REMOVE_ATTRIBUTION_FEATURE, DEATTRIBUTE),
                SELECT_FEATURE_PHENO(FILTER_ENTITY, PHENO_TAB),
            MARKER(ROOT),
                CUD_MARKER(MARKER),
                    CREATE_MARKER(CUD_MARKER),
                 MARKER_ATTRIBUTION(ADD_REMOVE_ATTRIBUTION),
                 MARKER_DEATTRIBUTION(ADD_REMOVE_ATTRIBUTION),
                 ATTRIBUTE_MARKER(ADD_REMOVE_ATTRIBUTION),
                 DEATTRIBUTE_MARKER(ADD_REMOVE_ATTRIBUTION),
                SELECT_GENE_FX(MARKER, FX_TAB, FILTER_ENTITY),
            FIGURE(ROOT),
                SELECT_FIGURE(FIGURE, FILTER_ENTITY),
                    SELECT_FIGURE_FX(SELECT_FIGURE, FX_TAB),
                    SELECT_FIGURE_PHENO(SELECT_FIGURE, PHENO_TAB),
                ADD_FIGURE(FIGURE),
            FISH(ROOT),
                CUD_FISH(FISH, FISH_TAB),
                    CREATE_FISH(CUD_FISH),
                    REMOVE_FISH(CUD_FISH),
                SELECT_FISH(FISH, FILTER_ENTITY),
                    SELECT_FISH_FX(SELECT_FISH, FX_TAB),
                    SELECT_FISH_PHENO(SELECT_FISH, PHENO_TAB),
                ADD_REMOVE_ATTRIBUTION_FISH(FISH),
                    ATTRIBUTE_FISH(ADD_REMOVE_ATTRIBUTION_FISH),
                    DEATTRIBUTE_FISH(ADD_REMOVE_ATTRIBUTION_FISH, DEATTRIBUTE),
                ADD_REMOVE_ATTRIBUTION_GENOTYPE(FISH),
                    ATTRIBUTE_GENOTYPE(ADD_REMOVE_ATTRIBUTION_GENOTYPE),
                    DEATTRIBUTE_GENOTYPE(ADD_REMOVE_ATTRIBUTION_GENOTYPE, DEATTRIBUTE),
            EXPERIMENT_CONDITION(ROOT),
                CUD_EXPERIMENT_CONDITION(EXPERIMENT_CONDITION, EXPERIMENT_TAB),
                    CREATE_EXPERIMENT_CONDITION(CUD_EXPERIMENT_CONDITION),
                    REMOVE_EXPERIMENT_CONDITION(CUD_EXPERIMENT_CONDITION),
                    UPDATE_EXPERIMENT_CONDITION(CUD_EXPERIMENT_CONDITION),
            EXPERIMENT(ROOT),
                CUD_EXPERIMENT(EXPERIMENT_CONDITION, EXPERIMENT_TAB),
                    CREATE_EXPERIMENT(CUD_EXPERIMENT),
                    REMOVE_EXPERIMENT(CUD_EXPERIMENT),
                    UPDATE_EXPERIMENT(CUD_EXPERIMENT),
            PUSH_TO_PATO(ROOT,FX_TAB),
            REMOVE_PHENTOTYPE_EXPERIMENT(PHENO_TAB),
    ;
        // @formatter:on

    private EventType[] parents = null;

    EventType() {
    }

    EventType(EventType... parents) {
        if (parents == null)
            return;
        this.parents = parents;
    }

    public boolean is(EventType type) {
        if (type == null)
            return false;
        if (this.equals(type))
            return true;
        for (EventType parent : parents) {
            if (parent.equals(type))
                return true;
            if (parent.hasParent()) {
                if (parent.is(type))
                    return true;
            }
        }
        return false;
    }

    private boolean hasParent() {
        return parents != null;
    }
}
