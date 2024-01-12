package org.zfin.feature.repository;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.feature.DnaMutationTerm;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HibernateControlledVocabularyRepositoryTest extends AbstractDatabaseTest {

    @Test
    public void getDnaTerm() {
        HibernateControlledVocabularyRepository<DnaMutationTerm> repo = new HibernateControlledVocabularyRepository<>(DnaMutationTerm.class);
        DnaMutationTerm term = repo.getControlledVocabularyTerm("SO:1000019");
        assertNotNull(term);
    }

    @Test
    public void getControlledVocabularyTermList() {
        HibernateControlledVocabularyRepository<DnaMutationTerm> repo = new HibernateControlledVocabularyRepository<>(DnaMutationTerm.class);
        List<DnaMutationTerm> termList = repo.getControlledVocabularyTermList();
        assertNotNull(termList);
        assertEquals(12, termList.size());
    }
}
