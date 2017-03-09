package org.zfin.marker.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.DataAlias;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.search.Category;
import org.zfin.search.FieldName;
import org.zfin.search.service.SolrService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/marker")
public class GeneAddController {

    private static Logger LOG = Logger.getLogger(GeneAddController.class);

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new GeneAddFormBeanValidator());
    }

    @ModelAttribute("formBean")
    public GeneAddFormBean getDefaultForm(@RequestParam(required = false) String type,
                                          @RequestParam(required = false) String source) {
        GeneAddFormBean form = new GeneAddFormBean();
        form.setPublicationId(source);
        form.setType(type);
        List<MarkerType> markerTypes = markerRepository.getMarkerTypesByGroup(Marker.TypeGroup.GENEDOM);
        Map<String, String> allTypes = new LinkedHashMap<>(markerTypes.size());
        for (MarkerType markerType : markerTypes) {
            if (!markerType.getDisplayName().equals("Transcript")) {
                allTypes.put(markerType.getType().name(), markerType.getDisplayName());
            }
        }

        allTypes.put(Marker.Type.EFG.name(), "Engineered Foreign Gene");
        form.setAllTypes(allTypes);

        return form;
    }

    @RequestMapping(value = "/gene-add", method = RequestMethod.GET)
    public String showGeneAddForm(Model model) throws IOException, SolrServerException {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add New Gene");
        return "marker/gene-add.page";
    }

    @RequestMapping(value = "/gene-add", method = RequestMethod.POST)
    public String processGeneAddForm(Model model,
                                     @Valid @ModelAttribute("formBean") GeneAddFormBean formBean,
                                     BindingResult result) throws IOException, SolrServerException {
        if (result.hasErrors()) {
            return showGeneAddForm(model);
        }

        Marker newGene = new Marker();
        newGene.setMarkerType(markerRepository.getMarkerTypeByName(formBean.getType()));
        newGene.setName(formBean.getName());
        newGene.setAbbreviation(formBean.getAbbreviation());
        newGene.setPublicComments(formBean.getPublicNote());

        Publication reference = publicationRepository.getPublication(formBean.getPublicationId());

        try {
            HibernateUtil.createTransaction();
            markerRepository.createMarker(newGene, reference);

            if (StringUtils.isNotEmpty(formBean.getAlias())) {
                markerRepository.addMarkerAlias(newGene, formBean.getAlias(), reference);
            }

            if (StringUtils.isNotEmpty(formBean.getCuratorNote())) {
                markerRepository.addMarkerDataNote(newGene, formBean.getCuratorNote());
            }

            HibernateUtil.flushAndCommitCurrentSession();

            Map<FieldName, Object> solrDoc = new HashMap<>(12);
            solrDoc.put(FieldName.ID, newGene.getZdbID());
            solrDoc.put(FieldName.CATEGORY, Category.GENE.getName());
            solrDoc.put(FieldName.TYPE, newGene.getMarkerType().getDisplayName());
            solrDoc.put(FieldName.NOTE, newGene.getPublicComments());
            solrDoc.put(FieldName.NAME, newGene.getAbbreviation());
            solrDoc.put(FieldName.PROPER_NAME, newGene.getAbbreviation());
            solrDoc.put(FieldName.GENE, newGene.getAbbreviation());
            solrDoc.put(FieldName.FULL_NAME, newGene.getName());
            solrDoc.put(FieldName.GENE_FULL_NAME, newGene.getName());
            solrDoc.put(FieldName.NAME_SORT, newGene.getAbbreviationOrder());
            solrDoc.put(FieldName.URL, "/" + newGene.getZdbID());
            solrDoc.put(FieldName.DATE, new Date());

            if (newGene.getAliases() != null) {
                List<String> aliases = newGene.getAliases().stream()
                        .map(DataAlias::getAlias)
                        .collect(Collectors.toList());
                solrDoc.put(FieldName.ALIAS, aliases);
                solrDoc.put(FieldName.GENE_PREVIOUS_NAME, aliases);
            }

            SolrService.addDocument(solrDoc);
        } catch (Exception e) {
            try {
                HibernateUtil.rollbackTransaction();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }

        return "redirect:/" + newGene.getZdbID();
    }

}
