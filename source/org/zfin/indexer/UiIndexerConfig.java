package org.zfin.indexer;

import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Log4j2
public enum UiIndexerConfig {

    FishModelIndexer("FishModel", FishModelIndexer.class, 10),
    GenesInvolvedIndexer("GenesInvolved", GenesInvolvedIndexer.class, 20),
    ChebiPhenotypeIndexer("ChebiPhenotype", ChebiPhenotypeIndexer.class, 30),
    PublicationExpressionIndexer("PublicationExpression", PublicationExpressionIndexer.class, 40),
    TermPhenotypeIndexer("TermPhenotype", TermPhenotypeIndexer.class, 50),
    // Note: ChebiFishModelIndexer depends on FishModelIndexer, i.e. it has to run after FishModelIndexer!
    ChebiFishModelIndexer("ChebiFishModel", ChebiFishModelIndexer.class, 60);

    private final String typeName;
    private final Class<?> indexClazz;

    private int order;

    UiIndexerConfig(String typeName, Class<?> indexClazz, int order) {
        this.typeName = typeName;
        this.indexClazz = indexClazz;
        this.order = order;
    }

    public String getTypeName() {
        return typeName;
    }

    public Class<?> getIndexClazz() {
        return indexClazz;
    }

    public static List<UiIndexerConfig> getAllIndexerSorted() {
        return Arrays.stream(values()).sorted(Comparator.comparing(config -> config.order)).toList();
    }

}
