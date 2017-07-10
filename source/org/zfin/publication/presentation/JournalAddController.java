package org.zfin.publication.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
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
import org.zfin.marker.presentation.SequenceTargetingReagentAddBean;
import org.zfin.marker.presentation.SequenceTargetingReagentAddBeanValidator;
import org.zfin.marker.presentation.SupplierLookupEntry;
import org.zfin.marker.presentation.TargetGeneLookupEntry;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.profile.Organization;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.publication.Journal;
import org.zfin.publication.Publication;
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
@RequestMapping("/publication")
public class JournalAddController {

    private static Logger LOG = Logger.getLogger(JournalAddController.class);

    private static MarkerRepository mr = RepositoryFactory.getMarkerRepository();
    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private static InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
    private static SequenceRepository sr = RepositoryFactory.getSequenceRepository();
    private static ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();



    @Autowired
    private HttpServletRequest request;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new JournalAddBeanValidator());
    }

    @RequestMapping(value = "/journal-add", method = RequestMethod.GET)
    protected String showForm(Model model) throws Exception {

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Journal");
        return "publication/journal-add.page";
    }

    @RequestMapping(value = "/journal-add", method = RequestMethod.POST)
    public String addJournal(Model model,
                                              @Valid @ModelAttribute("formBean") JournalAddBean formBean,
                                              BindingResult result) throws Exception {

        if (result.hasErrors()) {
            return showForm(model);
        }
        Journal newJournal= new Journal();
        String journalName = formBean.getName();
        String journalAbbrev=formBean.getAbbreviation();
        String printISSN=formBean.getPrintIssn();
        String eISSN=formBean.geteIssn();
        String nlmID=formBean.getNlmID();
        String publisher=formBean.getPublisher();
        Boolean isNice=formBean.isReproduceImages();



        try {
            HibernateUtil.createTransaction();
            Session session = HibernateUtil.currentSession();

            newJournal.setAbbreviation(journalAbbrev);
            newJournal.setName(journalName);
            newJournal.setNlmID(nlmID);
            newJournal.setPrintIssn(printISSN);
            newJournal.setOnlineIssn(eISSN);
            newJournal.setPublisher(publisher);
            newJournal.setIsNice(isNice);

            session.save(newJournal);


            String alias = formBean.getAlias();
            if (!StringUtils.isEmpty(alias)) {
                pr.addJournalAlias(newJournal, alias);
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

        return "redirect:/" + newJournal.getZdbID();
    }

    // looks up suppliers

}


