package org.zfin.anatomy.presentation;

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
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.repository.MutantRepository;
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
            boolean hasData = hasExpressionData(term);
            form.getSectionVisibility().setSectionData(AnatomySearchBean.Section.ANATOMY_EXPRESSION, hasData);
        }
        if (form.getSectionVisibility().isVisible(AnatomySearchBean.Section.ANATOMY_PHENOTYPE)) {
            form.getSectionVisibility().setSectionData(AnatomySearchBean.Section.ANATOMY_PHENOTYPE, true);
        } else {
            boolean hasData = hasPhenotypeData(term);
            form.getSectionVisibility().setSectionData(AnatomySearchBean.Section.ANATOMY_PHENOTYPE, hasData);
        }

        ModelAndView modelAndView = new ModelAndView("anatomy-item.page", LookupStrings.FORM_BEAN, form);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, term.getName());

        return modelAndView;
    }

    private boolean hasExpressionData(AnatomyItem anatomyTerm) {
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

    private boolean hasPhenotypeData(AnatomyItem anatomyTerm) {
        AnatomyStatistics statistics = anatomyRepository.getAnatomyStatisticsForMutants(anatomyTerm.getZdbID());
        if (statistics.getNumberOfObjects() > 0 || statistics.getNumberOfTotalDistinctObjects() > 0)
            return true;

        // check for wild type MOs
        PaginationResult<GenotypeExperiment> morphs =
                mutantRepository.getGenotypeExperimentMorhpolinosByAnatomy(anatomyTerm, true, AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        if (morphs != null && morphs.getTotalCount() > 0)
            return true;

        // check for non-wild-type MOs
        morphs =
                mutantRepository.getGenotypeExperimentMorhpolinosByAnatomy(anatomyTerm, false, AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        return morphs != null && morphs.getTotalCount() > 0;
    }

    private AnatomyItem retrieveAnatomyTermData(AnatomySearchBean form) {
        AnatomyItem ai;
        try {
            ai = anatomyRepository.getAnatomyTermByID(form.getAnatomyItem().getZdbID());
        }
        catch (Exception e) {
            LOG.error("failed to get anatomy term from form[" + form + "]");
            LOG.error("anatomyItem[" + form.getAnatomyItem() + "]");
            LOG.error("zdbID[" + form.getAnatomyItem().getZdbID() + "]");
            LOG.error("error", e);
            ai = null;
        }
        if (ai == null) {
            return null;
        }
        List<AnatomyRelationship> relationships = anatomyRepository.getAnatomyRelationships(ai);
        ai.setRelatedItems(relationships);
        form.setAnatomyItem(ai);
        return ai;
    }

    public void setAnatomyRepository(AnatomyRepository anatomyRepository) {
        this.anatomyRepository = anatomyRepository;
    }

    public void setMutantRepository(MutantRepository mutantRepository) {
        this.mutantRepository = mutantRepository;
    }

}
