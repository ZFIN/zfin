package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

/**
 * This Controller is used to pass a publication to the jsp page.
 */
@Controller
@RequestMapping("/devtool")
public class CurationTestController {

    @RequestMapping("/gwt/modules")
    protected String gwtModulesSummaryPage() throws Exception {
        return "dev-tools/gwt/modules";
    }

    @RequestMapping("/gwt/fx-curation")
    protected String fxCuration(@RequestParam(required = false) String publicationID,
                                Model model) throws Exception {

        if (StringUtils.isEmpty(publicationID)) {
            publicationID = "ZDB-PUB-990507-16";
        }
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(publicationID);
        model.addAttribute(publication);
        return "dev-tools/gwt/fx-curation";
    }

    @RequestMapping("/gwt/human-disease-curation")
    protected String humanDiseaseCuration(@RequestParam(required = false) String publicationID,
                                          Model model) throws Exception {

        if (StringUtils.isEmpty(publicationID)) {
            publicationID = "ZDB-PUB-990507-16";
        }
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(publicationID);
        model.addAttribute(publication);
        return "dev-tools/gwt/human-disease-curation";
    }

    @RequestMapping("/gwt/fish-tab-curation")
    protected String fishTabCuration(@RequestParam(required = false) String publicationID,
                                          Model model) throws Exception {

        if (StringUtils.isEmpty(publicationID)) {
            publicationID = "ZDB-PUB-990507-16";
        }
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(publicationID);
        model.addAttribute(publication);
        return "dev-tools/gwt/fish-tab-curation";
    }
    @RequestMapping("/gwt/construct-curation")
    protected String constructCuration(@RequestParam(required = false) String publicationID,
                                     Model model) throws Exception {

        if (StringUtils.isEmpty(publicationID)) {
            publicationID = "ZDB-PUB-990507-16";
        }
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(publicationID);
        model.addAttribute(publication);
        return "dev-tools/gwt/construct-curation";
    }

    @RequestMapping("/gwt/phenotype-curation")
    protected String phenotypeCuration(@RequestParam(required = false) String publicationID,
                                       Model model) throws Exception {

        if (StringUtils.isEmpty(publicationID)) {
            publicationID = "ZDB-PUB-961014-496";
        }
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(publicationID);
        model.addAttribute(publication);
        return "dev-tools/gwt/phenotype-curation";
    }

    @RequestMapping("/gwt/feature-curation")
    protected String featureCuration(@RequestParam(required = false) String publicationID,
                                     Model model) throws Exception {

        if (StringUtils.isEmpty(publicationID)) {
            publicationID = "ZDB-PUB-961014-496";
        }
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(publicationID);
        model.addAttribute(publication);
        return "dev-tools/gwt/feature-curation";
    }

    @RequestMapping("/gwt/go-curation")
    protected String goCuration(@RequestParam(required = false) String publicationID,
                                Model model) throws Exception {

        if (StringUtils.isEmpty(publicationID)) {
            publicationID = "ZDB-PUB-961014-496";
        }
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication(publicationID);
        model.addAttribute(publication);
        return "dev-tools/gwt/go-curation";
    }

}