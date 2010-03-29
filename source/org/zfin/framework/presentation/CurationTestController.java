package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.throwaway.ThrowawayController;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

/**
 * This Controller is used to pass a publication to the jsp page.
 */
public class CurationTestController extends SimplePassThroughController {

    private String publicationID;

    @Override
    public ModelAndView execute() throws Exception {
        if (StringUtils.isEmpty(publicationID))
            publicationID = "ZDB-PUB-961014-496";
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(publicationID);
        return new ModelAndView(viewName, "publication", publication);
    }

    public String getPublicationID() {
        return publicationID;
    }

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }
}