package org.zfin.curation.repository;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.curation.Curation;
import org.zfin.profile.Person;
import org.zfin.profile.repository.HibernateProfileRepository;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.HibernatePublicationRepository;
import org.zfin.publication.repository.PublicationRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CurationRepositoryTest extends AbstractDatabaseTest {

    private static CurationRepository curationRepository = new HibernateCurationRepository();
    private static PublicationRepository publicationRepository = new HibernatePublicationRepository();
    private static ProfileRepository profileRepository = new HibernateProfileRepository();

    @Test
    public void getCurationForPub() {
        Publication pub = publicationRepository.getPublication("ZDB-PUB-010102-2");
        List<Curation> curationList = curationRepository.getCurationForPub(pub);

        // it would be nice to assert something better, but since live data may change...
        assertThat(curationList, notNullValue());
    }

    @Test
    public void getOpenCurationForPub() {
        List<Curation> curationList = curationRepository.getOpenCurationTopics("ZDB-PUB-010102-2");

        assertThat(curationList, notNullValue());
        for (Curation curation : curationList) {
            assertThat(curation.getClosedDate(), is(nullValue()));
            assertThat(curation.getTopic(), is(not(Curation.Topic.LINKED_AUTHORS)));
        }
    }

    @Test
    public void closeCurationTopics() {
        Publication pub = publicationRepository.getPublication("ZDB-PUB-010102-2");
        Person curator = profileRepository.getPerson("ZDB-PERS-960805-676");

        curationRepository.closeCurationTopics(pub, curator);

        List<Curation> postList = curationRepository.getCurationForPub(pub);
        assertThat(postList, hasSize(Curation.Topic.values().length - 1)); // -1 because Linked Authors is skipped
        for (Curation curation : postList) {
            assertThat(curation.getClosedDate(), is(notNullValue()));
            assertThat(curation.getTopic(), is(not(Curation.Topic.LINKED_AUTHORS)));
        }
    }

}
