package org.zfin.marker.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.presentation.CreateAntibodyFormBeanValidator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.ui.PublicationSessionKey;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerSolrService;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.Category;
import org.zfin.wiki.service.AntibodyWikiWebService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@RequestMapping("/antibody")
public class AntibodyCreateController {


    @Autowired
    MarkerSolrService markerSolrService;

    private static Logger LOG = Logger.getLogger(AntibodyCreateController.class);
    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private static InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();

    @ModelAttribute("formBean")
    private CreateAntibodyFormBean getDefaultSearchForm(@RequestParam(value = "antibodyPublicationZdbID", required = false) String zdbID) {

        CreateAntibodyFormBean abBean = new CreateAntibodyFormBean();

        if (StringUtils.isNotEmpty(zdbID))
            abBean.setAntibodyPublicationZdbID(zdbID);

        return abBean;
    }

    @RequestMapping("/add")
    protected String showForm(Model model) throws Exception {

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Antibody");
        return "marker/antibody-add-form.page";
    }

    private @Autowired
    HttpServletRequest request;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new CreateAntibodyFormBeanValidator());
    }

    @RequestMapping(value = "/do-submit", method = RequestMethod.POST)
    public String createAntibody (Model model,
                              @Valid @ModelAttribute("formBean") CreateAntibodyFormBean formBean,
                              BindingResult result) throws Exception {

        if(result.hasErrors())
            return "marker/antibody-add-form.page";
        String antibodyName = formBean.getAntibodyName();

        Antibody newAntibody = new Antibody();
        newAntibody.setAbbreviation(antibodyName);
        newAntibody.setName(formBean.getAntibodyName());

        String pubZdbID = formBean.getAntibodyPublicationZdbID().trim();
        if (PublicationValidator.isShortVersion(pubZdbID))
            formBean.setAntibodyPublicationZdbID(PublicationValidator.completeZdbID(pubZdbID));
        else
            formBean.setAntibodyPublicationZdbID(pubZdbID);
        String zdbID = formBean.getAntibodyPublicationZdbID();
        Publication antibodyPub = pr.getPublication(zdbID);


        MarkerType mt = mr.getMarkerTypeByName(Marker.Type.ATB.toString());
        newAntibody.setMarkerType(mt);
        try {
            HibernateUtil.createTransaction();

            mr.createMarker(newAntibody, antibodyPub);
            PublicationService.addRecentPublications(request.getSession().getServletContext(), antibodyPub, PublicationSessionKey.ANTIBODY) ;

            HibernateUtil.flushAndCommitCurrentSession();

            HibernateUtil.currentSession().merge(newAntibody);
            createAntibodyWiki(newAntibody) ;
            markerSolrService.addMarkerStub(newAntibody, Category.ANTIBODY);

        } catch (Exception e) {
            try {
                HibernateUtil.rollbackTransaction();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }

        return "redirect:/action/marker/marker-edit?zdbID=" + newAntibody.getZdbID() + "&antibodyDefPubZdbID=" + antibodyPub.getZdbID();
    }

    private void createAntibodyWiki(Antibody antibody) {
        try {
            AntibodyWikiWebService.getInstance().createPageForAntibody(antibody);
        } catch (Exception e) {
            LOG.error("Unable to create antibody wiki: "+antibody,e);
        }
    }


}



