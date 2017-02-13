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
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.ui.PublicationSessionKey;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@RequestMapping("/marker")
public class EngineeredRegionAddController {

    private static Logger LOG = Logger.getLogger(EngineeredRegionAddController.class);
    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private static InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();

    @ModelAttribute("formBean")
    private EngineeredRegionAddBean getDefaultSearchForm(@RequestParam(value = "regionPublicationZdbID", required = false) String zdbID) {
        EngineeredRegionAddBean regionBean = new EngineeredRegionAddBean();

        if (StringUtils.isNotEmpty(zdbID))
            regionBean.setRegionPublicationZdbID(zdbID);

        return regionBean;

    }

    @RequestMapping("/engineeredRegion-add")
    protected String showForm(Model model) throws Exception {

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Engineered Region");
        return "marker/engineeredRegion-add.page";
    }

    private
    @Autowired
    HttpServletRequest request;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new EngineeredRegionAddBeanValidator());
    }

    @RequestMapping(value = "/region-do-submit", method = RequestMethod.POST)
    public String addRegion(@Valid @ModelAttribute("formBean") EngineeredRegionAddBean formBean,
                            BindingResult result) throws Exception {

        if (result.hasErrors())
            return "marker/engineeredRegion-add.page";

        String regionName = formBean.getRegionName();

        Marker newRegion = new Marker();
        newRegion.setAbbreviation(regionName.toLowerCase());
        newRegion.setName(formBean.getRegionName());
        newRegion.setPublicComments(formBean.getRegionComment());

        String pubZdbID = formBean.getRegionPublicationZdbID().trim();
        if (PublicationValidator.isShortVersion(pubZdbID))
            formBean.setRegionPublicationZdbID(PublicationValidator.completeZdbID(pubZdbID));
        else
            formBean.setRegionPublicationZdbID(pubZdbID);
        String zdbID = formBean.getRegionPublicationZdbID();
        Publication regionPub = pr.getPublication(zdbID);


        MarkerType mt = mr.getMarkerTypeByName(Marker.Type.REGION.toString());
        newRegion.setMarkerType(mt);
        try {
            HibernateUtil.createTransaction();

            mr.createMarker(newRegion, regionPub);
            PublicationService.addRecentPublications(request.getSession().getServletContext(), regionPub, PublicationSessionKey.GENE);

            HibernateUtil.flushAndCommitCurrentSession();

            HibernateUtil.currentSession().merge(newRegion);
        } catch (Exception e) {
            try {
                HibernateUtil.rollbackTransaction();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }

        Marker createdRegion = mr.getMarkerByName(newRegion.getName());
        String alias = formBean.getRegionAlias();

        if (!StringUtils.isEmpty(alias)) {
            if (!addRegionAlias(createdRegion, alias, regionPub).equalsIgnoreCase("successful")) {
                return "redirect:/action/dev-tools/test-error-page";
            }
        }

        String curationNote = formBean.getRegionCuratorNote();
        if (!StringUtils.isEmpty(curationNote)) {
            if (!addCuratorNote(createdRegion, curationNote).equalsIgnoreCase(curationNote)) {
                return "redirect:/action/dev-tools/test-error-page";
            }
        }

        return "redirect:/" + newRegion.getZdbID()+"#edit";
    }

    private String addRegionAlias(Marker marker, String alias, Publication pub) {
        try {
            HibernateUtil.createTransaction();

            mr.addMarkerAlias(marker, alias, pub);

            HibernateUtil.flushAndCommitCurrentSession();

            return "successful";
        } catch (Exception e) {
            try {
                HibernateUtil.rollbackTransaction();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }
    }

    private String addCuratorNote(Marker marker, String dNote) {
        try {
            HibernateUtil.createTransaction();
            DataNote dn = mr.addMarkerDataNote(marker, dNote);
            HibernateUtil.flushAndCommitCurrentSession();
            return dn.getNote();
        } catch (Exception e) {
            try {
                HibernateUtil.rollbackTransaction();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }
    }

}



