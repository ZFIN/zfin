package org.zfin.publication.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.publication.Journal;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;

import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/publication")
public class PublicationEditController {

    @Autowired
    private PublicationRepository publicationRepository;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // insert nulls instead of empty strings
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));

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
    public String showNewPublicationForm(Model model,
                                         @ModelAttribute PublicationForm form) {
        model.addAttribute("publicationForm", form);
        return "publication/add-publication.page";
    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String processNewPublication(@Valid @ModelAttribute PublicationForm publicationForm,
                                        BindingResult result,
                                        RedirectAttributes ra) {
        // the RedirectAttributes parameter keeps the query parameters off of the redirect URL
        // See: http://stackoverflow.com/questions/13247239/spring-mvc-controller-redirect-without-parameters-being-added-to-my-url

        if (result.hasErrors()) {
            return "publication/add-publication.page";
        }

        Publication publication = new Publication();
        PublicationService.applyFormToPublication(publication, publicationForm);
        publicationRepository.addPublication(publication);
        return "redirect:/" + publication.getZdbID();
    }

    @RequestMapping(value = "/{zdbID}/edit", method = RequestMethod.GET)
    public String showEditPublicationForm(Model model,
                                          @PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        model.addAttribute("publicationForm", PublicationService.getPublicationFormFromPublication(publication));
        return "publication/edit-publication.page";
    }

    @RequestMapping(value = "/{zdbID}/edit", method = RequestMethod.POST)
    public String processUpdatedPublication(@PathVariable String zdbID,
                                            @Valid @ModelAttribute PublicationForm publicationForm,
                                            BindingResult result,
                                            RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "publication/edit-publication.page";
        }

        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        PublicationService.applyFormToPublication(publication, publicationForm);
        publicationRepository.updatePublications(Arrays.asList(publication));
        return "redirect:/" + publication.getZdbID();
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
