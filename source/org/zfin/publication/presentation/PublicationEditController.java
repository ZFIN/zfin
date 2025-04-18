package org.zfin.publication.presentation;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
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
import org.zfin.datatransfer.webservice.NCBIEfetch;
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
import org.zfin.publication.PublicationType;
import org.zfin.publication.PubmedPublicationAuthor;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

@Controller
@RequestMapping("/publication")
public class PublicationEditController {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private PublicationService publicationService;

    private static Logger logger = LogManager.getLogger(PublicationEditController.class);


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
                    Journal journal = publicationRepository.getJournalByAbbreviation(text);
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
    }

    @InitBinder("publicationBean")
    public void initPublicationBinder(WebDataBinder binder) {
        binder.setValidator(new PublicationFormValidator());
    }


    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String showNewPublicationForm(@ModelAttribute PublicationBean publicationBean, Model model) {
        // default type should be journal
        Publication publication = new Publication();
        publication.setType(PublicationType.JOURNAL);
        publicationBean.setPublication(publication);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Publication");
        return "publication/add-publication";
    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String processNewPublication(Model model,
                                        @Valid @ModelAttribute PublicationBean publication,
                                        BindingResult result,
                                        RedirectAttributes ra) { // keeps the query parameters off of the redirect URL

        if (result.hasErrors()) {
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Publication");
            return "publication/add-publication";
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
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Add Publication");
            return "publication/add-publication";
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
        if (publication.getAccessionNumber() != null) {
            publicationBean.setAccessionNumber(publication.getAccessionNumber().toString());
        }

        model.addAttribute("publicationBean", publicationBean);
        model.addAttribute("allowCuration", publicationService.allowCuration(publication));
        model.addAttribute("hasCorrespondence", publicationService.hasCorrespondence(publication));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit Pub: " + publication.getTitle());
        return "publication/edit-publication";
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
            result.rejectValue("publication.canShowImages", "canShowImages.existingFigures");
        }

        if (result.hasErrors()) {
            return "publication/edit-publication";
        }

        HibernateUtil.createTransaction();
        try {
            Collection<BeanFieldUpdate> updates = publicationService.mergePublicationFromForm(publication, existingPublication);
            publicationRepository.updatePublications(Arrays.asList(existingPublication));
            for (BeanFieldUpdate update : updates) {
                infrastructureRepository.insertUpdatesTable(existingPublication, update, "Edit pub");
            }
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            model.addAttribute("error", "Error saving publication.");
            return "publication/edit-publication";
        }
        return "redirect:/" + existingPublication.getZdbID();
    }


    @ModelAttribute("statusList")
    public List<Publication.Status> getStatusList() {
        return Arrays.asList(Publication.Status.values());
    }

    @ModelAttribute("typeList")
    public List<PublicationType> getTypeList() {
        return Arrays.asList(PublicationType.values());
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
        if (publication.getAccessionNumber() != null) {
            bean.setAccessionNumber(publication.getAccessionNumber().toString());
        }
        model.addAttribute("publicationBean", bean);
        List<PubmedPublicationAuthor> authors = getPublicationRepository().getPubmedPublicationAuthorsByPublication(publication);

        if (authors != null && !authors.isEmpty()) {
            model.addAttribute("authorStrings", publicationService.getAuthorStringList(authors));
        } else {
            model.addAttribute("authorStrings", publicationService.splitAuthorListString(publication.getAuthors()));
        }
        model.addAttribute("allowCuration", publicationService.allowCuration(publication));
        model.addAttribute("hasCorrespondence", publicationService.hasCorrespondence(publication));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Link Authors: " + publication.getTitle());

        return "publication/link-authors";
    }

    @RequestMapping(value = "/{zdbID}/author-strings")
    @ResponseBody
    public List<String> authorStrings(@PathVariable String zdbID, HttpServletResponse response) {
        PublicationRepository publicationRepository = getPublicationRepository();


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

        List<PubmedPublicationAuthor> authors = getPublicationRepository().getPubmedPublicationAuthorsByPublication(publication);
        if (authors != null && !authors.isEmpty()) {
            return publicationService.getAuthorStringList(authors);
        } else {
            return publicationService.splitAuthorListString(publication.getAuthors());
        }
    }

    @RequestMapping(value = "/{zdbID}/registered-authors")
    @ResponseBody
    public List<PersonDTO> registeredAuthors(@PathVariable String zdbID, HttpServletResponse response) {

        PublicationRepository publicationRepository = getPublicationRepository();


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

    /**
     * Call this endpoint to force a refetch of a publication's listed authors from NCBI
     * @param zdbID the publication zdbID
     * @return a list of the added authors (returned to browser in json)
     */
    @RequestMapping(value = "/{zdbID}/refresh-listed-authors", method = RequestMethod.POST)
    @ResponseBody
    public List<PubmedPublicationAuthor> refreshListedAuthorsFromNCBI(@PathVariable String zdbID) {
        HibernateUtil.createTransaction();
        Publication publication = getPublicationRepository().getPublication(zdbID);
        List<NCBIEfetch.NameRecord> ncbiAuthors = NCBIEfetch.retrieveAuthorInfoForSinglePublication(publication);
        List<PubmedPublicationAuthor> zfinAuthors = getPublicationRepository().getPubmedPublicationAuthorsByPublication(publication);
        List<NCBIEfetch.NameRecord> newNcbiAuthors = ncbiAuthors.stream()
                .filter(nameRecord -> zfinAuthors.stream()
                        .noneMatch(author -> author.getLastName().equals(nameRecord.lastName()) &&
                                author.getFirstName().equals(nameRecord.firstName()) &&
                                author.getMiddleName().equals(nameRecord.middleName()) &&
                                author.getPubmedId().equals(nameRecord.accession())))
                .toList();
        List<PubmedPublicationAuthor> newZfinAuthors = newNcbiAuthors.stream()
                .map(nameRecord -> {
                    PubmedPublicationAuthor author = new PubmedPublicationAuthor();
                    author.setFirstName(nameRecord.firstName());
                    author.setMiddleName(nameRecord.middleName());
                    author.setLastName(nameRecord.lastName());
                    author.setPubmedId(nameRecord.accession());
                    author.setPublication(publication);
                    return author;
                })
                .toList();
        newZfinAuthors.forEach(nameRecord -> {
            HibernateUtil.currentSession().save(nameRecord);
        });
        HibernateUtil.flushAndCommitCurrentSession();
        return newZfinAuthors.stream().map(author -> {
            PubmedPublicationAuthor newAuthor = new PubmedPublicationAuthor();
            newAuthor.setFirstName(author.getFirstName());
            newAuthor.setMiddleName(author.getMiddleName());
            newAuthor.setLastName(author.getLastName());
            return newAuthor;
        }).toList();
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
        PublicationRepository publicationRepository = getPublicationRepository();
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
                if (tx != null) {
                    tx.rollback();
                }
            } catch (HibernateException hibernateException) {
                logger.error("Error during roll back of transaction", hibernateException);
            }
            logger.error("Error in Transaction", exception);
            throw new RuntimeException("Error during transaction. Rolled back.", exception);
        }


    }

    @RequestMapping(value = "/{zdbID}/removeAuthor/{personZdbID}")
    public void removeAuthor(@PathVariable String zdbID, @PathVariable String personZdbID, HttpServletResponse response) {
        PublicationRepository publicationRepository = getPublicationRepository();
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
                if (tx != null) {
                    tx.rollback();
                }
            } catch (HibernateException hibernateException) {
                logger.error("Error during roll back of transaction", hibernateException);
            }
            logger.error("Error in Transaction", exception);
            throw new RuntimeException("Error during transaction. Rolled back.", exception);
        }
    }

}
