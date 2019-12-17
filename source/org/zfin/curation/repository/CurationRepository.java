package org.zfin.curation.repository;

import org.zfin.curation.Curation;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.List;

public interface CurationRepository {

    List<Curation> getCurationForPub(Publication pub);

    List<Curation> getOpenCurationTopics(String pubZdbID);

    void closeCurationTopics(Publication pub, Person curator);

    void resetCurationTopics(Publication publication);

}
