package org.zfin.publication.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.CustomCalendarEditor;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.profile.service.BeanFieldUpdate;
import org.zfin.publication.Journal;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;

import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

@Controller
@RequestMapping("/publication")
public class PublicationEditController {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private PublicationService publicationService;

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
    public String showNewPublicationForm(@ModelAttribute Publication publication) {
        // default type should be journal
        publication.setType(Publication.Type.JOURNAL);
        return "publication/add-publication.page";
    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String processNewPublication(Model model,
                                        @Valid @ModelAttribute Publication publication,
                                        BindingResult result,
                                        RedirectAttributes ra) { // keeps the query parameters off of the redirect URL

        if (result.hasErrors()) {
            return "publication/add-publication.page";
        }

        Publication newPublication = new Publication();
        try {
            Collection<BeanFieldUpdate> updates = publicationService.mergePublicationFromForm(publication, newPublication);
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
        model.addAttribute("publication", publication);
        model.addAttribute("allowCuration", PublicationService.allowCuration(publication));
        return "publication/edit-publication.page";
    }

    @RequestMapping(value = "/{zdbID}/edit", method = RequestMethod.POST)
    public String processUpdatedPublication(Model model,
                                            @PathVariable String zdbID,
                                            @Valid @ModelAttribute Publication publication,
                                            BindingResult result,
                                            RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "publication/edit-publication.page";
        }

        Publication existingPublication = publicationRepository.getPublication(zdbID);
        if (existingPublication == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
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

}
