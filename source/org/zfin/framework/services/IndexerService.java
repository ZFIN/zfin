package org.zfin.framework.services;

import org.zfin.framework.dao.IndexerRunDAO;
import org.zfin.indexer.IndexerRun;

import jakarta.annotation.PostConstruct;

public class IndexerService extends BaseService<IndexerRun, IndexerRunDAO> {

    IndexerRunDAO indexerRunDAO;

    @PostConstruct
    protected void init() {
        setSQLDao(indexerRunDAO);
    }
}
