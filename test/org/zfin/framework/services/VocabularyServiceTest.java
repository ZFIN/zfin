package org.zfin.framework.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.feature.Feature;
import org.zfin.feature.service.FeatureAttributionService;
import org.zfin.framework.Vocabulary;
import org.zfin.framework.VocabularyTerm;
import org.zfin.framework.api.SearchResponse;
import org.zfin.framework.dao.VocabularyDAO;
import org.zfin.framework.dao.VocabularyTermDAO;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@WebAppConfiguration
public class VocabularyServiceTest extends AbstractDatabaseTest  {

    @Test
    public void vocabularyTermTest() {

        VocabularyService service = new VocabularyService();

        VocabularyTerm vocab = service.getVocabularyTerm(VocabularyEnum.TRANSCRIPT_ANNOTATION_METHOD, "Ensembl");

        assertNotNull("allele record should be found", vocab);
    }

}

