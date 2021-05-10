package org.zfin.feature.repository;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.feature.DnaMutationTerm;

import static org.junit.Assert.assertNotNull;

public class HibernateControlledVocabularyRepositoryTest extends AbstractDatabaseTest {

    @Test
    public void getDnaTerm() {
        HibernateControlledVocabularyRepository<DnaMutationTerm> repo = new HibernateControlledVocabularyRepository<>(DnaMutationTerm.class);
        DnaMutationTerm term = repo.getControlledVocabularyTerm("SO:1000019");
        assertNotNull(term);
    }
}
