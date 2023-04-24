package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public enum UiIndexerConfig {

    FishModelIndexer("fishModel", FishModelIndexer.class),
    PublicationExpressionIndexer("publicationExpression", PublicationExpressionIndexer.class),
    GenesInvolvedIndexer("GenesInvolved", GenesInvolvedIndexer.class),
    ChebiPhenotypeIndexer("ChebiPhenotype", ChebiPhenotypeIndexer.class),
    TermPhenotypeIndexer("TermPhenotype", TermPhenotypeIndexer.class)
    ;

    private final String typeName;
    private final Class<?> indexClazz;

    UiIndexerConfig(String typeName, Class<?> indexClazz) {
        this.typeName = typeName;
        this.indexClazz = indexClazz;
    }

    public String getTypeName() {
        return typeName;
    }

    public Class<?> getIndexClazz() {
        return indexClazz;
    }

}
