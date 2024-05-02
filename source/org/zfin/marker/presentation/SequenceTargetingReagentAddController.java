package org.zfin.marker.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupEntry;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.ui.PublicationSessionKey;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.marker.service.MarkerSolrService;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.profile.Organization;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.Category;
import org.zfin.sequence.STRMarkerSequence;
import org.zfin.sequence.repository.SequenceRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/marker")
public class SequenceTargetingReagentAddController {

    private static Logger LOG = LogManager.getLogger(SequenceTargetingReagentAddController.class);

    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private static MutantRepository mur = RepositoryFactory.getMutantRepository();
    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private static InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
    private static SequenceRepository sr = RepositoryFactory.getSequenceRepository();
    private static ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();

    @Autowired
    private ResourceBundleMessageSource messageSource;

    @Autowired
    MarkerSolrService markerSolrService;

    @Autowired
    private HttpServletRequest request;

    @ModelAttribute("formBean")
    private SequenceTargetingReagentAddBean getDefaultSearchForm(@RequestParam(value = "sequenceTargetingReagentType", required = false) String type,
                                                                 @RequestParam(value = "sequenceTargetingReagentPublicationZdbID", required = false) String pubZdbID) {
        SequenceTargetingReagentAddBean sequenceTargetingReagentBean = new SequenceTargetingReagentAddBean();

        Map<String, String> strTypes = getStrTypesMap();
        sequenceTargetingReagentBean.setStrTypes(strTypes);

        sequenceTargetingReagentBean.setStrType(type);

        if (StringUtils.isNotEmpty(pubZdbID)) {
            sequenceTargetingReagentBean.setPublicationID(pubZdbID);
        }

        return sequenceTargetingReagentBean;
    }

    @InitBinder("formBean")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new SequenceTargetingReagentAddBeanValidator());
    }

    @RequestMapping(value = "/sequence-targeting-reagent-add", method = RequestMethod.GET)
    protected String showForm(Model model) throws Exception {
        String strTypesJson = new ObjectMapper().writeValueAsString(getStrTypesMap()).replaceAll("\"", "&quot;");
        model.addAttribute("strTypesJson", strTypesJson);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Sequence Targeting Reagent");
        return "marker/sequence-targeting-reagent-add";
    }

    @RequestMapping(value = "/sequence-targeting-reagent-add", method = RequestMethod.POST)
    public String addSequenceTargetingReagentReact(Model model,
                                              @Valid @ModelAttribute("formBean") SequenceTargetingReagentAddBean formBean,
                                              BindingResult result) throws Exception {

        if (result.hasErrors()) {
            model.addAttribute("fieldErrorsJson",
                    new ObjectMapper().writeValueAsString(
                            mapFieldErrors(result.getFieldErrors())).replaceAll("\"", "&quot;"));
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

        Marker.Type markerTypeEnum = Marker.Type.getTypeByPrefix(formBean.getStrType());
        MarkerType mt = mr.getMarkerTypeByName(markerTypeEnum.toString());
        newSequenceTargetingReagent.setMarkerType(mt);

        try {
            HibernateUtil.createTransaction();
            mr.createMarker(newSequenceTargetingReagent, sequenceTargetingReagentPub);
            PublicationService.addRecentPublications(request.getSession().getServletContext(), sequenceTargetingReagentPub, PublicationSessionKey.GENE);

            String alias = formBean.getAlias();
            if (!StringUtils.isEmpty(alias)) {
                mr.addMarkerAlias(newSequenceTargetingReagent, alias, sequenceTargetingReagentPub);
            }

            String curationNote = formBean.getCuratorNote();
            if (!StringUtils.isEmpty(curationNote)) {
                mr.addMarkerDataNote(newSequenceTargetingReagent, curationNote);
            }

            List<String> targetGeneAbbrs = formBean.getTargetGeneSymbols();
            if (targetGeneAbbrs == null) {
                targetGeneAbbrs = Collections.emptyList();
            }
            for (String targetGeneAbbr : targetGeneAbbrs) {
                Marker targetGene = mr.getMarkerByAbbreviation(targetGeneAbbr);

                if (targetGene != null && !StringUtils.isEmpty(pubZdbID)) {
                    if (targetGene.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                        MarkerService.addMarkerRelationship(newSequenceTargetingReagent, targetGene, pubZdbID, MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
                    }
                    if (targetGene.isInTypeGroup(Marker.TypeGroup.NONTSCRBD_REGION)) {
                        if (newSequenceTargetingReagent.getType() == Marker.Type.CRISPR) {
                            MarkerService.addMarkerRelationship(newSequenceTargetingReagent, targetGene, pubZdbID, MarkerRelationship.Type.CRISPR_TARGETS_REGION);
                        }
                        if (newSequenceTargetingReagent.getType() == Marker.Type.TALEN) {
                            MarkerService.addMarkerRelationship(newSequenceTargetingReagent, targetGene, pubZdbID, MarkerRelationship.Type.TALEN_TARGETS_REGION);
                        }
                    }
                }
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

            if (formBean.isReversed2() || formBean.isComplemented2()) {
                String note = MarkerService.getSTRModificationNote(formBean.getReportedSequence2(), formBean.isReversed2(), formBean.isComplemented2());
                mr.addMarkerDataNote(newSequenceTargetingReagent, note);
            }

            HibernateUtil.flushAndCommitCurrentSession();

            markerSolrService.addMarkerStub(newSequenceTargetingReagent, Category.SEQUENCE_TARGETING_REAGENT);

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
    List<LookupEntry> lookupTargetGenes(@RequestParam("term") String lookupString) {
        return mr.getGeneSuggestionList(lookupString);
    }

    @RequestMapping(value = "/find-relationshipTargets", method = RequestMethod.GET)
    public
    @ResponseBody
    List<LookupEntry> lookupRelationshipTargets(@RequestParam("term") String lookupString) {

        return mr.getRelationshipTargetsForString(lookupString);
    }

    @RequestMapping(value = "/propose-name-by-type-and-genes", method = RequestMethod.GET)
    public
    @ResponseBody
    String proposeNameByTypeAndGenes(@RequestParam("type") String type, @RequestParam("genes") String genes) {
        Set<String> geneSet = Set.of(genes.split(","));
        List<Marker> strList = mr.getMarkerWithRelationshipsBySecondMarkers(geneSet);

        Marker.Type strType = Marker.Type.getTypeByPrefix(type);

        //filtered by type
        strList = strList.stream().filter(str -> str.getMarkerType().getName().equals(strType.toString())).toList();

        Integer maxIndex = 0;
        if (!strList.isEmpty()) {
            //use regex to extract number from eg. CRISPR3-abc,def,ghi
            maxIndex = strList.stream().map(
                            (str) -> {
                                Pattern pattern = Pattern.compile("[A-Z]+(\\d+)");
                                Matcher matcher = pattern.matcher(str.getAbbreviation());
                                matcher.find();
                                return matcher.group(1);
                            })
                    .map(Integer::parseInt)
                    .max(Integer::compareTo).orElse(0);
        }
        return "" + type + (maxIndex + 1) + "-" + geneSet.stream().sorted().collect(Collectors.joining(","));
    }

    @RequestMapping(value = "/name-exists", method = RequestMethod.GET)
    public
    @ResponseBody
    String proposeNameByTypeAndGenes(@RequestParam("name") String name) {
        Marker marker = mr.getMarkerByName(name);
        if (marker != null) {
            return "Name already exists: " + marker.getAbbreviation() + " (" + marker.getZdbID() + ")";
        }
        return "";
    }

    private static Map<String, String> getStrTypesMap() {
        Map<String, String> strTypes = new HashMap<>(3);
        strTypes.put(Marker.Type.CRISPR.getPrefix(), "CRISPR");
        strTypes.put(Marker.Type.MRPHLNO.getPrefix(), "Morpholino");
        strTypes.put(Marker.Type.TALEN.getPrefix(), "TALEN");
        return strTypes;
    }

    /**
     * Translating fieldErrors to json for the client.
     * The result should be something like: [{message: "error message...", rejectedValue: "the bad value", field: "publicationID"},{...},...]
     * @param fieldErrors
     * @return a list of maps (each map to be translated to a json object)
     */
    private List<Map<String, String>> mapFieldErrors(List<FieldError> fieldErrors) {
        List<Map<String,String>> mappedErrors = new ArrayList<>();
        for (FieldError error : fieldErrors) {
            Map<String, String> map = new HashMap<>();
            String message = messageSource.getMessage(error.getCode(), error.getArguments(), Locale.getDefault());
            map.put("message", message);
            if (!StringUtils.isEmpty((String)error.getRejectedValue())) {
                map.put("rejectedValue", (String)error.getRejectedValue());
            }
            map.put("field", error.getField());
            mappedErrors.add(map);
        }
        return mappedErrors;
    }
}


