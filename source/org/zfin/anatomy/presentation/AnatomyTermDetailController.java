package org.zfin.anatomy.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;

/**
 * Controller class that serves the anatomy term detail page.
 */
@Controller
public class AnatomyTermDetailController {

    private static final Logger LOG = Logger.getLogger(AnatomyTermDetailController.class);

    @Autowired
    private AntibodyRepository antibodyRepository;
    @Autowired
    private AnatomyRepository anatomyRepository;
    @Autowired
    private MutantRepository mutantRepository;
    @Autowired
    private OntologyRepository ontologyRepository;
    private static PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();

    @RequestMapping(value = "/anatomy-preview/{zdbID}")
    public String getAnatomyPreview(Model model,
                                    @PathVariable("zdbID") String zdbID,
                                    AnatomySearchBean defaultFormBean
    ) throws Exception {
        LOG.info("Start Anatomy Term Detail Controller");

        AnatomyItem term = retrieveAnatomyTermData(defaultFormBean, zdbID);
        if (term == null) {
            return LookupStrings.idNotFound(model, zdbID);
        }

        model.addAttribute(LookupStrings.FORM_BEAN, defaultFormBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.ANATOMY.getTitleString() + term.getTermName());

        return "anatomy/anatomy-preview.ajax";
    }

    @RequestMapping(value = "/anatomy-view/{zdbID}")
    public String getAnatomyView(Model model
            , @ModelAttribute("formBean") AnatomySearchBean form
            , @PathVariable("zdbID") String zdbID
    ) throws Exception {
        LOG.info("Start Anatomy Term Detail Controller");

        AnatomyItem term = retrieveAnatomyTermData(form, zdbID);
        if (term == null) {
            return LookupStrings.idNotFound(model, zdbID);
        }

        if (form.getSectionVisibility().isVisible(AnatomySearchBean.Section.ANATOMY_EXPRESSION)) {
            form.getSectionVisibility().setSectionData(AnatomySearchBean.Section.ANATOMY_EXPRESSION, true);
        } else {
            // check if there are any data in this section.
            boolean hasData = hasExpressionData(form.getAoTerm());
            form.getSectionVisibility().setSectionData(AnatomySearchBean.Section.ANATOMY_EXPRESSION, hasData);
        }
        if (form.getSectionVisibility().isVisible(AnatomySearchBean.Section.ANATOMY_PHENOTYPE)) {
            form.getSectionVisibility().setSectionData(AnatomySearchBean.Section.ANATOMY_PHENOTYPE, true);
        } else {
            boolean hasData = hasPhenotypeData(term);
            form.getSectionVisibility().setSectionData(AnatomySearchBean.Section.ANATOMY_PHENOTYPE, hasData);
        }
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.ANATOMY.getTitleString() + term.getTermName());

        return "anatomy/anatomy-view.page";
    }

    private boolean hasExpressionData(GenericTerm anatomyTerm) {
        AnatomyStatistics statistics = anatomyRepository.getAnatomyStatistics(anatomyTerm.getZdbID());
        if (statistics.getNumberOfObjects() > 0 || statistics.getNumberOfTotalDistinctObjects() > 0)
            return true;
        // check for antibody records including substructures
        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(1);
        int numOfAntibodies = antibodyRepository.getAntibodyCount(anatomyTerm, true);
        if (numOfAntibodies > 0)
            return true;

        // check for in situ probes
        PaginationResult<HighQualityProbe> hqp = publicationRepository.getHighQualityProbeNames(anatomyTerm, 1);
        if (hqp != null && hqp.getTotalCount() > 0)
            return true;

        return false;
    }

    private boolean hasPhenotypeData(AnatomyItem anatomyTerm) {
        GenericTerm term = ontologyRepository.getTermByOboID(anatomyTerm.getOboID());
        AnatomyStatistics statistics = anatomyRepository.getAnatomyStatisticsForMutants(term.getZdbID());
        if (statistics != null && (statistics.getNumberOfObjects() > 0 || statistics.getNumberOfTotalDistinctObjects() > 0))
            return true;

        // check for MOs
        List<GenotypeExperiment> morphs =
                mutantRepository.getGenotypeExperimentMorpholinos(term, null);
        return morphs != null && morphs.size() > 0;
    }

    protected AnatomyItem retrieveAnatomyTermData(AnatomySearchBean form, String aoTermID) {
        AnatomyItem ai = null;
        try {
            GenericTerm term = null;
            if (aoTermID != null && aoTermID.startsWith("ZFA") && form.getId() == null) {
                form.setId(aoTermID);
                aoTermID = null;
            }
            if (aoTermID != null) {
                if (aoTermID.contains(ActiveData.Type.TERM.name())) {
                    term = ontologyRepository.getTermByZdbID(aoTermID);
                    if (term == null) {
                        LOG.error("Failed to find term for Term ID: " + aoTermID);
                        return null;
                    }
                    ai = anatomyRepository.getAnatomyTermByOboID(term.getOboID());
                } else {
                    ai = anatomyRepository.getAnatomyTermByID(aoTermID);
                }
                // ToDo: This should make the above retrieval of the term from the anatomy_item table superfluous.
                // For now we still use it to serve this page.
                if (ai == null) {
                    LOG.error("Unable to retrieve anatomy item for term ID: " + aoTermID);
                    return null;
                }
                form.setAnatomyItem(ai);
                term = ontologyRepository.getTermByOboID(ai.getOboID());
            } else {
                String id = form.getId();
                if (StringUtils.isEmpty(id)) {
                    return null;
                } else if (id.startsWith("ZFA")) {
                    term = ontologyRepository.getTermByOboID(id);
                }
                if (term != null) {
                    ai = anatomyRepository.getAnatomyTermByOboID(term.getOboID());
                }
            }
            form.setAoTerm(term);
        } catch (Exception
                e) {
            LOG.error("Failed to get anatomy term detail from " + form + "]: ", e);
            ai = null;
        }
        if (ai == null) {
            return null;
        }
        List<AnatomyRelationship> relationships = anatomyRepository.getAnatomyRelationships(ai);
        ai.setRelatedItems(relationships);
        form.setAnatomyItem(ai);
        Term term = ontologyRepository.getTermByOboID(ai.getOboID());
        ai.setImages(term.getImages());

        return ai;
    }

}
