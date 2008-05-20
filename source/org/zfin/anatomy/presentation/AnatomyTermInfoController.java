package org.zfin.anatomy.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyRelationship;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.Figure;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.marker.MarkerStatistic;
import org.zfin.marker.presentation.ExpressedGeneDisplay;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.GenotypeStatistics;
import org.zfin.mutant.presentation.MorpholinoStatistics;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Controller class that serves the anatomy term detail page.
 */
public class AnatomyTermInfoController extends AbstractCommandController {

    private static final Logger LOG = Logger.getLogger(AnatomyTermDetailController.class);

    private AnatomyRepository anatomyRepository;

    public AnatomyTermInfoController() {
        setCommandClass(AnatomySearchBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        LOG.info("Start Anatomy Term Detail Controller");
        AnatomySearchBean form = (AnatomySearchBean) command;
        AnatomyItem term = retrieveAnatomyTermData(form);
        if (term == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, form.getAnatomyItem().getZdbID());


        ModelAndView modelAndView = new ModelAndView("anatomy-terminfo.page", LookupStrings.FORM_BEAN, form);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, term.getName());

        return modelAndView;
    }


    private AnatomyItem retrieveAnatomyTermData(AnatomySearchBean form) {
        AnatomyItem ai = anatomyRepository.getAnatomyTermByID(form.getAnatomyItem().getZdbID());
        if (ai == null)
            return null;
        List<AnatomyRelationship> relationships = anatomyRepository.getAnatomyRelationships(ai);
        ai.setRelatedItems(relationships);
        form.setAnatomyItem(ai);
        return ai;
    }



    public void setAnatomyRepository(AnatomyRepository anatomyRepository) {
        this.anatomyRepository = anatomyRepository;
    }

}