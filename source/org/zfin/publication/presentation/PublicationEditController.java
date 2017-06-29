package org.zfin.publication.presentation;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.gwt.root.dto.PersonDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.CustomCalendarEditor;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.profile.Person;
import org.zfin.profile.service.BeanFieldUpdate;
import org.zfin.publication.Journal;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

@Controller
@RequestMapping("/publication")
public class PublicationEditController {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private PublicationService publicationService;

    private static Logger logger = Logger.getLogger(PublicationEditController.class);


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // insert nulls instead of empty strings
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        binder.registerCustomEditor(GregorianCalendar.class, new CustomCalendarEditor(dateFormat));

        binder.registerCustomEditor(Journal.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (!text.isEmpty()) {
                    Journal journal = publicationRepository.findJournalByAbbreviation(text);
                    if (journal == null) {
                        throw new IllegalArgumentException("Could not find journal with abbreviation " + text);
                    }
                    setValue(journal);
                }
            }

            @Override
            public String getAsText() {
                Journal journal = (Journal) getValue();
                return (journal == null) ? "" : journal.getAbbreviation();
            }
        });

        binder.setValidator(new PublicationFormValidator());
    }


    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String showNewPublicationForm(@ModelAttribute PublicationBean publicationBean) {
        // default type should be journal
        Publication publication = new Publication();
        publication.setType(Publication.Type.JOURNAL);
        publicationBean.setPublication(publication);
        return "publication/add-publication.page";
    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String processNewPublication(Model model,
                                        @Valid @ModelAttribute PublicationBean publication,
                                        BindingResult result,
                                        RedirectAttributes ra) { // keeps the query parameters off of the redirect URL

        if (result.hasErrors()) {
            return "publication/add-publication.page";
        }

        Publication newPublication = new Publication();
        try {
            Collection<BeanFieldUpdate> updates = publicationService.mergePublicationFromForm(publication.getPublication(), newPublication);
            publicationRepository.addPublication(newPublication);
            for (BeanFieldUpdate update : updates) {
                infrastructureRepository.insertUpdatesTable(newPublication, update, "Add pub");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error saving publication.");
            return "publication/add-publication.page";
        }
        return "redirect:/" + newPublication.getZdbID();
    }

    @RequestMapping(value = "/{zdbID}/edit", method = RequestMethod.GET)
    public String showEditPublicationForm(Model model,
                                          @PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        PublicationBean publicationBean = new PublicationBean();
        publicationBean.setPublication(publication);
        if (publication.getAccessionNumber() != null)
            publicationBean.setAccessionNumber(publication.getAccessionNumber().toString());

        model.addAttribute("publicationBean", publicationBean);
        model.addAttribute("allowCuration", PublicationService.allowCuration(publication));
        model.addAttribute("hasCorrespondence", PublicationService.hasCorrespondence(publication));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit Pub: " + publication.getTitle());
        return "publication/edit-publication.page";
    }

    @RequestMapping(value = "/{zdbID}/edit", method = RequestMethod.POST)
    public String processUpdatedPublication(Model model,
                                            @PathVariable String zdbID,
                                            @Valid @ModelAttribute PublicationBean publicationBean,
                                            BindingResult result,
                                            RedirectAttributes ra) {
        Publication existingPublication = publicationRepository.getPublication(zdbID);

        if (existingPublication == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        Publication publication = publicationBean.getPublication();
        boolean hasFigureWithImages = existingPublication.getFigures().stream().anyMatch(f -> !f.isImgless());
        if (!publication.isCanShowImages() && existingPublication.isCanShowImages() && hasFigureWithImages) {
            result.rejectValue("canShowImages", "canShowImages.existingFigures");
        }

        if (result.hasErrors()) {
            return "publication/edit-publication.page";
        }

        try {
            Collection<BeanFieldUpdate> updates = publicationService.mergePublicationFromForm(publication, existingPublication);
            publicationRepository.updatePublications(Arrays.asList(existingPublication));
            for (BeanFieldUpdate update : updates) {
                infrastructureRepository.insertUpdatesTable(publication, update, "Edit pub");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error saving publication.");
            return "publication/edit-publication.page";
        }
        return "redirect:/" + existingPublication.getZdbID();
    }


    @ModelAttribute("statusList")
    public List<Publication.Status> getStatusList() {
        return Arrays.asList(Publication.Status.values());
    }

    @ModelAttribute("typeList")
    public List<Publication.Type> getTypeList() {
        return Arrays.asList(Publication.Type.values());
    }

    @RequestMapping(value = "/{zdbID}/link")
    public String linkAuthors(@PathVariable String zdbID, Model model, HttpServletResponse response) {
        Publication publication = publicationRepository.getPublication(zdbID);
        //try zdb_replaced data if necessary
        if (publication == null) {
            String replacedZdbID = getInfrastructureRepository().getReplacedZdbID(zdbID);
            if (replacedZdbID != null) {
                publication = publicationRepository.getPublication(replacedZdbID);
            }
        }

        //give up
        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        PublicationBean bean = new PublicationBean();
        bean.setPublication(publication);
        if (publication.getAccessionNumber() != null)
            bean.setAccessionNumber(publication.getAccessionNumber().toString());
        model.addAttribute("publicationBean", bean);
        model.addAttribute("authorStrings", publicationService.splitAuthorListString(publication.getAuthors()));
        model.addAttribute("allowCuration", PublicationService.allowCuration(publication));
        model.addAttribute("hasCorrespondence", PublicationService.hasCorrespondence(publication));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Link Authors: " + publication.getTitle());

        return "publication/link-authors.page";
    }

    @RequestMapping(value = "/{zdbID}/author-strings")
    @ResponseBody
    public List<String> authorStrings(@PathVariable String zdbID, HttpServletResponse response) {
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();


        Publication publication = publicationRepository.getPublication(zdbID);
        //try zdb_replaced data if necessary
        if (publication == null) {
            String replacedZdbID = getInfrastructureRepository().getReplacedZdbID(zdbID);
            if (replacedZdbID != null) {
                publication = publicationRepository.getPublication(replacedZdbID);
            }
        }

        //give up
        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return null;
        }


        return publicationService.splitAuthorListString(publication.getAuthors());
    }

    @RequestMapping(value = "/{zdbID}/registered-authors")
    @ResponseBody
    public List<PersonDTO> registeredAuthors(@PathVariable String zdbID, HttpServletResponse response) {

        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();


        Publication publication = publicationRepository.getPublication(zdbID);
        //try zdb_replaced data if necessary
        if (publication == null) {
            String replacedZdbID = getInfrastructureRepository().getReplacedZdbID(zdbID);
            if (replacedZdbID != null) {
                publication = publicationRepository.getPublication(replacedZdbID);
            }
        }

        //give up
        if (publication == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }


        List<PersonDTO> authorList = new ArrayList<>();

        for (Person person : publication.getPeople()) {
            authorList.add(DTOConversionService.convertToPersonDTO(person));
        }

        return authorList;
    }

    @RequestMapping(value = "/link-author-suggestions")
    @ResponseBody
    public List<PersonDTO> linkAuthorSuggestions(@RequestParam String authorString) {
        List<PersonDTO> personDTOList = new ArrayList<>();
        for (Person person : publicationService.getAuthorSuggestions(authorString)) {
            personDTOList.add(DTOConversionService.convertToPersonDTO(person));
        }
        return personDTOList;
    }

    @RequestMapping(value = "/{zdbID}/addAuthor/{personZdbID}")
    public void addAuthor(@PathVariable String zdbID, @PathVariable String personZdbID, HttpServletResponse response) {
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
        Session session = HibernateUtil.currentSession();

        Publication publication = publicationRepository.getPublication(zdbID);
        //try zdb_replaced data if necessary
        if (publication == null) {
            String replacedZdbID = getInfrastructureRepository().getReplacedZdbID(zdbID);
            if (replacedZdbID != null) {
                publication = publicationRepository.getPublication(replacedZdbID);
            }
        }

        //give up
        if (publication == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Person person = RepositoryFactory.getProfileRepository().getPerson(personZdbID);
        if (person == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            publication.getPeople().add(person);

            HibernateUtil.currentSession().save(publication);
            tx.commit();
        } catch (Exception exception) {
            try {
                if (tx != null)
                    tx.rollback();
            } catch (HibernateException hibernateException) {
                logger.error("Error during roll back of transaction", hibernateException);
            }
            logger.error("Error in Transaction", exception);
            throw new RuntimeException("Error during transaction. Rolled back.", exception);
        }


    }

    @RequestMapping(value = "/{zdbID}/removeAuthor/{personZdbID}")
    public void removeAuthor(@PathVariable String zdbID, @PathVariable String personZdbID, HttpServletResponse response) {
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
        Session session = HibernateUtil.currentSession();

        Publication publication = publicationRepository.getPublication(zdbID);
        //try zdb_replaced data if necessary
        if (publication == null) {
            String replacedZdbID = getInfrastructureRepository().getReplacedZdbID(zdbID);
            if (replacedZdbID != null) {
                publication = publicationRepository.getPublication(replacedZdbID);
            }
        }

        //give up
        if (publication == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Person person = RepositoryFactory.getProfileRepository().getPerson(personZdbID);
        if (person == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }


        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            publication.getPeople().remove(person);

            HibernateUtil.currentSession().save(publication);
            tx.commit();
        } catch (Exception exception) {
            try {
                if (tx != null)
                    tx.rollback();
            } catch (HibernateException hibernateException) {
                logger.error("Error during roll back of transaction", hibernateException);
            }
            logger.error("Error in Transaction", exception);
            throw new RuntimeException("Error during transaction. Rolled back.", exception);
        }
    }

}
