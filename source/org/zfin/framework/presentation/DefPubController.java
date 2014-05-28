package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationBean;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

/**
 * Controller to populate the Def-Pub section.
 */
@Controller
public class DefPubController {

    @RequestMapping("/def-pub")
    protected String showLookupTestPage(@ModelAttribute("formBean") PublicationBean bean, BindException errors) throws Exception {
        String pubID = bean.getZdbID();
        if (StringUtils.isEmpty(pubID))
            return "def-pub.page";

        PublicationValidator.validatePublicationID(pubID, "zdbID", errors);
        if (errors.hasErrors())
            return "def-pub.page";

        pubID = pubID.trim();
        if (PublicationValidator.isShortVersion(pubID))
            bean.setZdbID(PublicationValidator.completeZdbID(pubID));
        else
            bean.setZdbID(pubID);

        PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
        Publication publication = pubRepository.getPublication(bean.getZdbID());
        if (publication != null) {
            bean.setValidPublication(true);
            bean.setPublication(publication);
        } else {
            bean.setZdbID(pubID);
        }

        return "def-pub.page";
    }
}
