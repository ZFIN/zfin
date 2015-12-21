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
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.profile.Organization;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.STRMarkerSequence;
import org.zfin.sequence.repository.SequenceRepository;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/marker")
public class SequenceTargetingReagentAddController {

    private static Logger LOG = Logger.getLogger(SequenceTargetingReagentAddController.class);

    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private static InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
    private static SequenceRepository sr = RepositoryFactory.getSequenceRepository();
    private static ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();

    @ModelAttribute("formBean")
    private SequenceTargetingReagentAddBean getDefaultSearchForm(@RequestParam(value = "sequenceTargetingReagentType", required = false) String type,
                                                                 @RequestParam(value = "sequenceTargetingReagentPublicationZdbID", required = false) String pubZdbID) {
        SequenceTargetingReagentAddBean sequenceTargetingReagentBean = new SequenceTargetingReagentAddBean();

        Map<String, String> strTypes = new HashMap<>(3);
        strTypes.put(Marker.Type.CRISPR.name(), "CRISPR");
        strTypes.put(Marker.Type.MRPHLNO.name(), "Morpholino");
        strTypes.put(Marker.Type.TALEN.name(), "TALEN");
        sequenceTargetingReagentBean.setStrTypes(strTypes);

        sequenceTargetingReagentBean.setStrType(type);

        if (StringUtils.isNotEmpty(pubZdbID)) {
            sequenceTargetingReagentBean.setPublicationID(pubZdbID);
        }

        return sequenceTargetingReagentBean;
    }

    @Autowired
    private HttpServletRequest request;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new SequenceTargetingReagentAddBeanValidator());
    }

    @RequestMapping(value = "/sequence-targeting-reagent-add", method = RequestMethod.GET)
    protected String showForm(Model model) throws Exception {

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Sequence Targeting Reagent");
        return "marker/sequence-targeting-reagent-add.page";
    }

    @RequestMapping(value = "/sequence-targeting-reagent-add", method = RequestMethod.POST)
    public String addSequenceTargetingReagent(Model model,
                                              @Valid @ModelAttribute("formBean") SequenceTargetingReagentAddBean formBean,
                                              BindingResult result) throws Exception {

        if (result.hasErrors()) {
            return showForm(model);
        }

        String sequenceTargetingReagentName = formBean.getName();

        STRMarkerSequence newSequenceTargetingReagentSequence = new STRMarkerSequence();
        SequenceTargetingReagent newSequenceTargetingReagent = new SequenceTargetingReagent();
        newSequenceTargetingReagentSequence.setSequence(formBean.getSequence().toUpperCase());
        newSequenceTargetingReagentSequence.setType("Nucleotide");
        newSequenceTargetingReagent.setSequence(newSequenceTargetingReagentSequence);

        if (formBean.getStrType().equalsIgnoreCase("TALEN")) {
            String sequenceTargetingReagentSecondSequence = formBean.getSequence2();
            newSequenceTargetingReagentSequence.setSecondSequence(sequenceTargetingReagentSecondSequence.toUpperCase());
        }

        newSequenceTargetingReagent.setName(sequenceTargetingReagentName);
        newSequenceTargetingReagent.setAbbreviation(sequenceTargetingReagentName);
        newSequenceTargetingReagent.setPublicComments(formBean.getPublicNote());

        String pubZdbID = formBean.getPublicationID().trim();
        if (PublicationValidator.isShortVersion(pubZdbID)) {
            formBean.setPublicationID(PublicationValidator.completeZdbID(pubZdbID));
        } else {
            formBean.setPublicationID(pubZdbID);
        }
        Publication sequenceTargetingReagentPub = pr.getPublication(formBean.getPublicationID());

        MarkerType mt = mr.getMarkerTypeByName(formBean.getStrType());
        newSequenceTargetingReagent.setMarkerType(mt);

        try {
            HibernateUtil.createTransaction();
            mr.createMarker(newSequenceTargetingReagent, sequenceTargetingReagentPub);
            ir.insertUpdatesTable(newSequenceTargetingReagent, "new " + formBean.getStrType(), "");
            PublicationService.addRecentPublications(request.getSession().getServletContext(), sequenceTargetingReagentPub, PublicationSessionKey.GENE);

            String alias = formBean.getAlias();
            if (!StringUtils.isEmpty(alias)) {
                mr.addMarkerAlias(newSequenceTargetingReagent, alias, sequenceTargetingReagentPub);
            }

            String curationNote = formBean.getCuratorNote();
            if (!StringUtils.isEmpty(curationNote)) {
                mr.addMarkerDataNote(newSequenceTargetingReagent, curationNote);
            }

            String targetGeneAbbr = formBean.getTargetGeneSymbol();
            Marker targetGene = mr.getGeneByAbbreviation(targetGeneAbbr);

            if (targetGene != null && !StringUtils.isEmpty(pubZdbID)) {
                MarkerService.addMarkerRelationship(newSequenceTargetingReagent, targetGene, pubZdbID, MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
            }

            String supplierName = formBean.getSupplier();
            if (!StringUtils.isEmpty(supplierName)) {
                Organization supplier = profileRepository.getOrganizationByName(formBean.getSupplier());
                profileRepository.addSupplier(supplier, newSequenceTargetingReagent);
            }

            if (formBean.isReversed() || formBean.isComplemented()) {
                String note = MarkerService.getSTRModificationNote(formBean.getReportedSequence(), formBean.isReversed(), formBean.isComplemented());
                mr.addMarkerDataNote(newSequenceTargetingReagent, note);
            }

            if (formBean.isReversed2() || formBean.isReversed2()) {
                String note = MarkerService.getSTRModificationNote(formBean.getReportedSequence2(), formBean.isReversed2(), formBean.isComplemented2());
                mr.addMarkerDataNote(newSequenceTargetingReagent, note);
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

        return "redirect:/" + newSequenceTargetingReagent.getZdbID();
    }

    // looks up suppliers
    @RequestMapping(value = "/find-suppliers", method = RequestMethod.GET)
    public
    @ResponseBody
    List<SupplierLookupEntry> lookupSuppliers(@RequestParam("term") String lookupString) {
        return mr.getSupplierNamesForString(lookupString);
    }

    // looks up target genes
    @RequestMapping(value = "/find-targetGenes", method = RequestMethod.GET)
    public
    @ResponseBody
    List<TargetGeneLookupEntry> lookupTargetGenes(@RequestParam("term") String lookupString) {
        return mr.getTargetGenesWithNoTranscriptForString(lookupString);
    }
}


