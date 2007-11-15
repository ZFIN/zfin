package org.zfin.infrastructure;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.zfin.sequence.DBLink;
import org.zfin.repository.RepositoryFactory;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;
import java.util.HashSet;


public class AttributionService {
    static Logger logger = Logger.getLogger(AttributionService.class);

    /**
     * Does this piece of data have a journal publication as one of it's attributions?
     *
     * Code is a little
     *
     * @param data activedata object
     * @return true for noncuration pubs
     */
    public static boolean dataSupportedOnlyByCurationPubs(ActiveData data) {
        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();

        List<RecordAttribution> attribs = infrastructureRepository.getRecordAttributions(data);

        for (RecordAttribution attrib : attribs) {
            Publication pub = publicationRepository.getPublication(attrib.getSourceZdbID());
            if (!StringUtils.equals(pub.getType(),Publication.CURATION))
                return false;
        }
        return true;
    }

    /**
     * The method accepting an activedata object is what we should use, but it's a pain
     * if we have to go out to a repository to get an active data object in all sorts of
     * unrelated places, so here's a convenience method
     * @param zdbID
     * @return
     */
    public static boolean dataSupportedOnlyByCurationPubs(String zdbID) {
        ActiveData data = RepositoryFactory.getInfrastructureRepository().getActiveData(zdbID);
        return dataSupportedOnlyByCurationPubs(data);
    }

}

