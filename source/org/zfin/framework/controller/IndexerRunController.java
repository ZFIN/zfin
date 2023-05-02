package org.zfin.framework.controller;

import org.alliancegenome.curation_api.services.base.BaseOntologyTermService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.api.ObjectResponse;
import org.zfin.framework.api.SearchResponse;
import org.zfin.framework.dao.IndexerRunDAO;
import org.zfin.framework.interfaces.BaseCrudInterface;
import org.zfin.framework.services.BaseEntityCrudService;
import org.zfin.framework.services.IndexerService;
import org.zfin.indexer.IndexerRun;

import java.util.HashMap;

@RequestMapping("/api/indexer")
//public class IndexerRunController<S extends BaseEntityCrudService<E, IndexerRunDAO>, E extends IndexerRun, D extends IndexerRunDAO> implements BaseCrudInterface<IndexerRun> {
public class IndexerRunController {

    IndexerService indexerService;

    public ObjectResponse<IndexerRun> create(IndexerRun entity) {
        return null;
    }

    public SearchResponse<IndexerRun> find(Integer page, Integer limit, HashMap<String, Object> params) {
        return null;
    }
}
