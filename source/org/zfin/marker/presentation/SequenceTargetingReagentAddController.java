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
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.profile.Organization;
import org.zfin.profile.repository.*;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.STRMarkerSequence;

import org.zfin.sequence.repository.SequenceRepository;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

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

        sequenceTargetingReagentBean.setSequenecTargetingReagentType(type);

        if (StringUtils.isNotEmpty(pubZdbID))
            sequenceTargetingReagentBean.setSequenecTargetingReagentPublicationID(pubZdbID);

        return sequenceTargetingReagentBean;

    }

    @RequestMapping("/sequence-targeting-reagent-add")
    protected String showForm(Model model) throws Exception {

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Sequence Targeting Reagent");
        return "marker/sequence-targeting-reagent-add.page";
    }

    private @Autowired
    HttpServletRequest request;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new SequenceTargetingReagentAddBeanValidator());
    }

    @RequestMapping(value = "/sequence-targeting-reagent-do-submit", method = RequestMethod.POST)
    public String addSequenceTargetingReagent (Model model,
                                @Valid @ModelAttribute("formBean") SequenceTargetingReagentAddBean formBean,
                                BindingResult result) throws Exception {

        if(result.hasErrors())
            return "marker/sequence-targeting-reagent-add.page";

        String sequenceTargetingReagentName = formBean.getSequenecTargetingReagentName();

        STRMarkerSequence newSequenceTargetingReagentSequence = new STRMarkerSequence();
        SequenceTargetingReagent newSequenceTargetingReagent = new SequenceTargetingReagent() ;
        newSequenceTargetingReagentSequence.setSequence(formBean.getSequenecTargetingReagentSequence().toUpperCase());
        newSequenceTargetingReagentSequence.setType("Nucleotide");
        newSequenceTargetingReagent.setSequence(newSequenceTargetingReagentSequence);

        if (formBean.getSequenecTargetingReagentType().equalsIgnoreCase("TALEN")) {
            String sequenceTargetingReagentSecondSequence = formBean.getSequenecTargetingReagentSecondSequence();
            newSequenceTargetingReagentSequence.setSecondSequence(sequenceTargetingReagentSecondSequence.toUpperCase());
        }

        newSequenceTargetingReagent.setName(sequenceTargetingReagentName);
        newSequenceTargetingReagent.setAbbreviation(sequenceTargetingReagentName);
        newSequenceTargetingReagent.setPublicComments(formBean.getSequenecTargetingReagentComment());

        String pubZdbID = formBean.getSequenecTargetingReagentPublicationID().trim();
        if (PublicationValidator.isShortVersion(pubZdbID))
            formBean.setSequenecTargetingReagentPublicationID(PublicationValidator.completeZdbID(pubZdbID));
        else
            formBean.setSequenecTargetingReagentPublicationID(pubZdbID);
        Publication sequenceTargetingReagentPub = pr.getPublication(formBean.getSequenecTargetingReagentPublicationID());

        MarkerType mt = new MarkerType();
        if (formBean.getSequenecTargetingReagentType().equalsIgnoreCase("Morpholino")) {
            mt = mr.getMarkerTypeByName(Marker.Type.MRPHLNO.toString());
        } else if (formBean.getSequenecTargetingReagentType().equalsIgnoreCase("TALEN")) {
            mt = mr.getMarkerTypeByName(Marker.Type.TALEN.toString());
        } else if (formBean.getSequenecTargetingReagentType().equalsIgnoreCase("CRISPR")) {
            mt = mr.getMarkerTypeByName(Marker.Type.CRISPR.toString());
        }
        newSequenceTargetingReagent.setMarkerType(mt);
        // set marker sequence component



        try {
            HibernateUtil.createTransaction();
            mr.createMarker(newSequenceTargetingReagent, sequenceTargetingReagentPub);
            ir.insertUpdatesTable(newSequenceTargetingReagent, "new " + formBean.getSequenecTargetingReagentType(), "");
            PublicationService.addRecentPublications(request.getSession().getServletContext(), sequenceTargetingReagentPub, PublicationSessionKey.GENE) ;

            String alias = formBean.getSequenecTargetingReagentAlias();
            if(!StringUtils.isEmpty(alias)) {
                mr.addMarkerAlias(newSequenceTargetingReagent, alias, sequenceTargetingReagentPub);
            }

            String curationNote = formBean.getSequenecTargetingReagentCuratorNote();
            if(!StringUtils.isEmpty(curationNote)) {
                mr.addMarkerDataNote(newSequenceTargetingReagent, curationNote);
            }

            String targetGeneAbbr = formBean.getTargetGeneSymbol();
            Marker targetGene = mr.getGeneByAbbreviation(targetGeneAbbr);

            if (targetGene != null && !StringUtils.isEmpty(pubZdbID)) {
                MarkerService.addMarkerRelationship(newSequenceTargetingReagent, targetGene, pubZdbID, MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
            }

            String supplierName = formBean.getSequenecTargetingReagentSupplierName();
            if (!StringUtils.isEmpty(supplierName)) {
                Organization supplier = profileRepository.getOrganizationByName(formBean.getSequenecTargetingReagentSupplierName());
                profileRepository.addSupplier(supplier, newSequenceTargetingReagent);
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
                "?MIval=aa-markerview.apg&UPDATE=1&orgOID=&OID=" + newSequenceTargetingReagent.getZdbID();
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


