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
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.profile.Person;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerSequence;
import org.zfin.sequence.repository.SequenceRepository;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
public class DisruptorAddController {

    private static Logger LOG = Logger.getLogger(DisruptorAddController.class);
    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private static InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
    private static SequenceRepository sr = RepositoryFactory.getSequenceRepository();

    @ModelAttribute("formBean")
    private DisruptorAddBean getDefaultSearchForm(@RequestParam(value = "disruptorType", required = true) String type,
                                                  @RequestParam(value = "disruptorPublicationZdbID", required = false) String pubZdbID) {
        DisruptorAddBean disruptorBean = new DisruptorAddBean();

        disruptorBean.setDisruptorType(type);

        if (StringUtils.isNotEmpty(pubZdbID))
            disruptorBean.setDisruptorPublicationZdbID(pubZdbID);

        return disruptorBean;

    }

    @RequestMapping("/disruptor-add")
    protected String showForm(Model model) throws Exception {

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Sequence Targeting Reagent");
        return "marker/disruptor-add.page";
    }

    private @Autowired
    HttpServletRequest request;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new DisruptorAddBeanValidator());
    }

    @RequestMapping(value = "/disruptor-do-submit", method = RequestMethod.POST)
    public String addDisruptor (Model model,
                                @Valid @ModelAttribute("formBean") DisruptorAddBean formBean,
                                BindingResult result) throws Exception {

        if(result.hasErrors())
            return "marker/disruptor-add.page";

        String disruptorName = formBean.getDisruptorName();

        Person currentUser = Person.getCurrentSecurityUser();
        SequenceTargetingReagent newDisruptor = new SequenceTargetingReagent();
        newDisruptor.setName(disruptorName);
        newDisruptor.setAbbreviation(disruptorName);

        newDisruptor.setPublicComments(formBean.getDisruptorComment());
        newDisruptor.setOwner(currentUser);

        String pubZdbID = formBean.getDisruptorPublicationZdbID().trim();
        if (PublicationValidator.isShortVersion(pubZdbID))
            formBean.setDisruptorPublicationZdbID(PublicationValidator.completeZdbID(pubZdbID));
        else
            formBean.setDisruptorPublicationZdbID(pubZdbID);
        Publication disruptorPub = pr.getPublication(formBean.getDisruptorPublicationZdbID());

        MarkerType mt = new MarkerType();
        if (formBean.getDisruptorType().equalsIgnoreCase("Morpholino")) {
            mt = mr.getMarkerTypeByName(Marker.Type.MRPHLNO.toString());
        } else if (formBean.getDisruptorType().equalsIgnoreCase("TALEN")) {
            mt = mr.getMarkerTypeByName(Marker.Type.TALEN.toString());
        } else if (formBean.getDisruptorType().equalsIgnoreCase("CRISPR")) {
            mt = mr.getMarkerTypeByName(Marker.Type.CRISPR.toString());
        }
        newDisruptor.setMarkerType(mt);
        // set marker sequence component
        MarkerSequence newDisruptorSequence = new MarkerSequence();
        newDisruptorSequence.setSequence(formBean.getDisruptorSequence().toUpperCase());
        if (formBean.getDisruptorType().equalsIgnoreCase("TALEN")) {
            String disruptorSecondSequence = formBean.getDisruptorSecondSequence();
            newDisruptorSequence.setSecondSequence(disruptorSecondSequence.toUpperCase());
        }
        newDisruptorSequence.setType("Nucleotide");
        newDisruptor.setSequence(newDisruptorSequence);

        try {
            HibernateUtil.createTransaction();
            mr.createMarker(newDisruptor, disruptorPub);
            ir.insertUpdatesTable(newDisruptor, "new " + formBean.getDisruptorType(), "", currentUser);
            PublicationService.addRecentPublications(request.getSession().getServletContext(), disruptorPub, PublicationSessionKey.GENE) ;

            String alias = formBean.getDisruptorAlias();
            if(!StringUtils.isEmpty(alias)) {
                mr.addMarkerAlias(newDisruptor, alias, disruptorPub);
            }

            String curationNote = formBean.getDisruptorCuratorNote();
            if(!StringUtils.isEmpty(curationNote)) {
                mr.addMarkerDataNote(newDisruptor, curationNote, currentUser);
            }

            String targetGeneAbbr = formBean.getTargetGeneSymbol();
            Marker targetGene = mr.getGeneByAbbreviation(targetGeneAbbr);

            if (targetGene != null && !StringUtils.isEmpty(pubZdbID)) {
                MarkerService.addMarkerRelationship(newDisruptor, targetGene, pubZdbID, MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
            }

            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            try {
                HibernateUtil.rollbackTransaction();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }

        return "redirect:/" + ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value() +
                "?MIval=aa-markerview.apg&UPDATE=1&orgOID=&OID=" + newDisruptor.getZdbID();
    }

}


