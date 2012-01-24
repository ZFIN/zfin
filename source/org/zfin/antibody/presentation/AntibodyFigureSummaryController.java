package org.zfin.antibody.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyService;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.expression.ExpressionSummaryCriteria;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.AddPublicationValidator;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.util.Map;

import static org.zfin.repository.RepositoryFactory.getAnatomyRepository;
import static org.zfin.repository.RepositoryFactory.getAntibodyRepository;

/**
 * Controller class that serves a figure summary page for a given antibody and labeling structure,
 * i.e. for a given antibody, superterm:subterm, start and end stage it lists all figures and publications.
 * Optionally it serves figures with images only or all.
 */
@Controller
public class AntibodyFigureSummaryController {

    private static final Logger LOG = Logger.getLogger(AntibodyFigureSummaryController.class);

    @Autowired
    private OntologyRepository ontologyRepository;

    @Autowired
    private AntibodyRepository antibodyRepository;

    @ModelAttribute("formBean")
    private AntibodyBean getFormBean() {
        return new AntibodyBean();
    }

    @RequestMapping("/antibody-figure-summary")
    protected String showForm(@RequestParam(value = "superTermID", required = true) String supertermID,
                              @RequestParam(value = "subTermID", required = false) String subtermID,
                              @RequestParam(value = "antibodyID", required = true) String antibodyID,
                              @RequestParam(value = "startStageID", required = false) String startStageID,
                              @RequestParam(value = "endStageID", required = false) String endStageID,
                              @RequestParam(value = "figuresWithImg", required = false) Boolean figuresWithImg,
                              @ModelAttribute("formBean") AntibodyBean form,
                              Model model) throws Exception {

        LOG.info("Start Antibody Figure Summary Controller");

        Antibody ab = antibodyRepository.getAntibodyByID(antibodyID);
        if (ab == null)
            return "record-not-found.page";

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Antibody figure summary: " + ab.getName());
        GenericTerm superterm = ontologyRepository.getTermByZdbID(supertermID);
        form.setSuperTerm(superterm);

        GenericTerm subterm = null;
        if (StringUtils.isNotEmpty(subtermID)) {
            subterm = ontologyRepository.getTermByZdbID(subtermID);
            form.setSubTerm(subterm);
        }

        DevelopmentStage startStage = getAnatomyRepository().getStageByID(startStageID);
        form.setStartStage(startStage);

        DevelopmentStage endStage = getAnatomyRepository().getStageByID(endStageID);
        form.setEndStage(endStage);

        AntibodyService abStat = new AntibodyService(ab);

        ExpressionSummaryCriteria criteria = abStat.createExpressionSummaryCriteria(superterm, subterm, startStage, endStage, figuresWithImg);
        form.setExpressionSummaryCriteria(criteria);
        abStat.createFigureSummary(criteria);
        form.setOnlyFiguresWithImg(figuresWithImg);
        form.setAntibodyStat(abStat);
        form.setAntibody(ab);

        return "antibody/antibody-figure-summary.page";
    }

    @RequestMapping("/antibody-publication-list")
    public String antibodyCitationList(@RequestParam(value = "antibodyID", required = true) String antibodyID,
                                       @ModelAttribute("formBean") AntibodyBean bean,
                                       Model model) throws Exception {

/*
        if (antibodyID == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, "EMPTY");
*/
        Antibody ab = getAntibodyRepository().getAntibodyByID(antibodyID);
/*
        if (ab == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, bean.getAntibody().getZdbID());
*/

        bean.setAntibody(ab);
        return "antibody/antibody-publication-list.page";
    }

    @RequestMapping("/antibody-citation-disassociate-publication")
    public String disassociatePublicationFromAntibody(@RequestParam(value = "antibodyID", required = true) String antibodyID,
                                                      @ModelAttribute("formBean") AntibodyBean bean,
                                                      Model model) throws Exception {
        Antibody ab = antibodyRepository.getAntibodyByID(antibodyID);

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
            ir.removeRecordAttributionForData(ab.getZdbID(), bean.getDisassociatedPubId());
            Person currentUser = Person.getCurrentSecurityUser();
            ir.insertUpdatesTable(ab, "antibody attribution", "", currentUser);
            tx.commit();
        } catch (Exception exception) {
            try {
                tx.rollback();
            } catch (HibernateException hibernateException) {
                LOG.error("Error during roll back of transaction", hibernateException);
            }
            LOG.error("Error in Transaction", exception);
            throw new RuntimeException("Error during transaction. Rolled back.", exception);
        }

        bean.setAntibody(ab);
        return "antibody/antibody-publication-list.page";
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new AddPublicationValidator());
    }

    @RequestMapping("/antibody-citation-associate-publication")
    public String associatePublicationFromAntibody(@Valid @ModelAttribute("formBean") AntibodyBean bean,
                                                   BindingResult errors,
                                                   Model model) throws Exception {
        Antibody ab = antibodyRepository.getAntibodyByID(bean.getEntityID());
        bean.setAntibody(ab);
        if (errors.getErrorCount() > 0)
            return "antibody/antibody-publication-list.page";

        String pubID = bean.getAntibodyNewPubZdbID();
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        Publication publication = pr.getPublication(pubID);
        Transaction tx = null;

        try {
            tx = HibernateUtil.createTransaction();
            RepositoryFactory.getMarkerRepository().addMarkerPub(ab, publication);
            Person currentUser = Person.getCurrentSecurityUser();
            InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
            ir.insertUpdatesTable(ab, "antibody attribution", "", currentUser);
            tx.commit();
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }

        bean.setAntibody(ab);
        bean.setAntibodyNewPubZdbID("");
        return "antibody/antibody-publication-list.page";
    }

}
