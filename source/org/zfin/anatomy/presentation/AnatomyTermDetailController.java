package org.zfin.anatomy.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.Term;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Controller class that serves the anatomy term detail page.
 */
public class AnatomyTermDetailController extends AbstractCommandController {

    private static final Logger LOG = Logger.getLogger(AnatomyTermDetailController.class);

    private static AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
    private static AnatomyRepository anatomyRepository;
    private static MutantRepository mutantRepository;
    private static PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();

    public AnatomyTermDetailController() {
        setCommandClass(AnatomySearchBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LOG.info("Start Anatomy Term Detail Controller");
        AnatomySearchBean form = (AnatomySearchBean) command;
        AnatomyItem term = retrieveAnatomyTermData(form);
        if (term == null) {
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, form.getAnatomyItem().getZdbID());
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

        ModelAndView modelAndView = new ModelAndView("anatomy-item.page", LookupStrings.FORM_BEAN, form);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, term.getTermName());

        return modelAndView;
    }

    private boolean hasExpressionData(Term anatomyTerm) {
        AnatomyStatistics statistics = anatomyRepository.getAnatomyStatistics(anatomyTerm.getZdbID());
        if (statistics.getNumberOfObjects() > 0 || statistics.getNumberOfTotalDistinctObjects() > 0)
            return true;
        // check for antibody records including substructures
        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(1);
        PaginationResult<Antibody> antibodies = antibodyRepository.getAntibodiesByAOTerm(anatomyTerm, pagination, true);
        if (antibodies != null && antibodies.getTotalCount() > 0)
            return true;

        // check for in situ probes
        PaginationResult<HighQualityProbe> hqp = publicationRepository.getHighQualityProbeNames(anatomyTerm, 1);
        if (hqp != null && hqp.getTotalCount() > 0)
            return true;

        return false;
    }

    private boolean hasPhenotypeData(Term anatomyTerm) {
        AnatomyStatistics statistics = anatomyRepository.getAnatomyStatisticsForMutants(anatomyTerm.getZdbID());
        if (statistics != null && (statistics.getNumberOfObjects() > 0 || statistics.getNumberOfTotalDistinctObjects() > 0))
            return true;

        // check for wild type MOs
        PaginationResult<GenotypeExperiment> morphs =
                mutantRepository.getGenotypeExperimentMorpholinos(anatomyTerm, true, AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        if (morphs != null && morphs.getTotalCount() > 0)
            return true;

        // check for non-wild-type MOs
        morphs =
                mutantRepository.getGenotypeExperimentMorpholinos(anatomyTerm, false, AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        return morphs != null && morphs.getTotalCount() > 0;
    }

    protected AnatomyItem retrieveAnatomyTermData(AnatomySearchBean form) {
        AnatomyItem ai = null;
        try {
            String aoTermID = form.getAnatomyItem().getZdbID();
            Term term = null;
            if (aoTermID != null && aoTermID.startsWith("ZFA") && form.getId() == null) {
                form.setId(aoTermID);
                aoTermID = null;
            }
            if (aoTermID != null) {
                if (aoTermID.contains(ActiveData.Type.TERM.name())) {
                    term = OntologyManager.getInstance().getTermByID(Ontology.ANATOMY, aoTermID);
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
                term = RepositoryFactory.getOntologyRepository().getTermByOboID(ai.getOboID());
            } else {
                String id = form.getId();
                if (StringUtils.isEmpty(id))
                    return null;
                if (id.contains(ActiveData.Type.TERM.name())) {
                    term = OntologyManager.getInstance().getTermByID(Ontology.ANATOMY, id);
                } else if (id.startsWith("ZFA")) {
                    term = RepositoryFactory.getOntologyRepository().getTermByOboID(id);
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
        Term term = RepositoryFactory.getOntologyRepository().getTermByOboID(ai.getOboID());
        ai.setImages(term.getImages());

        return ai;
    }

    public void setAnatomyRepository(AnatomyRepository anatomyRepository) {
        this.anatomyRepository = anatomyRepository;
    }

    public void setMutantRepository(MutantRepository mutantRepository) {
        this.mutantRepository = mutantRepository;
    }

}
