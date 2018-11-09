package org.zfin.zebrashare.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.profile.Lab;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationFileType;
import org.zfin.publication.PublicationTrackingLocation;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.publication.repository.PublicationRepository;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.GregorianCalendar;

@Controller
@RequestMapping("/zebrashare")
public class SubmissionFormController {

    @Autowired
    private PublicationRepository publicationRepository;

    private static final Logger LOG = Logger.getLogger(SubmissionFormController.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(new SubmissionFormValidator());
    }

    @ModelAttribute("labOptions")
    private Collection<Lab> getLabOptions() {
        Person user = ProfileService.getCurrentSecurityUser();
        if (user == null) {
            return null;
        }
        HibernateUtil.currentSession().refresh(user);
        return user.getLabs();
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String showSubmissionForm(@ModelAttribute("formBean") SubmissionFormBean formBean) {
        Person user = ProfileService.getCurrentSecurityUser();
        if (user != null) {
            if (StringUtils.isEmpty(formBean.getSubmitterName())) {
                formBean.setSubmitterName(user.getDisplay());
            }
            if (StringUtils.isEmpty(formBean.getSubmitterEmail())) {
                formBean.setSubmitterEmail(user.getEmail());
            }
        }
        return "zebrashare/new-submission.page";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home(@ModelAttribute("formBean") SubmissionFormBean formBean) {
        return "zebrashare/home.page";
    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String processSubmissionForm(@Valid @ModelAttribute("formBean") SubmissionFormBean formBean,
                                        BindingResult result) {
        if (result.hasErrors()) {
            for (ObjectError error : result.getAllErrors()) {
                LOG.error(error.toString());
            }
            return "zebrashare/new-submission.page";
        }

        Publication publication = new Publication();
        publication.setTitle(formBean.getTitle());
        publication.setAuthors(formBean.getAuthors());
        publication.setAbstractText(formBean.getAbstractText());
        publication.setZebrasharePublic(false);
        publication.setJournal(publicationRepository.findJournalByAbbreviation("zebraShare"));
        publication.setType(Publication.Type.JOURNAL);
        publication.setEntryDate(new GregorianCalendar());
        publicationRepository.addPublication(publication, PublicationTrackingStatus.Name.READY_FOR_CURATION, PublicationTrackingLocation.Name.ZEBRASHARE);
        try {
            publicationRepository.addPublicationFile(
                    publication,
                    publicationRepository.getPublicationFileTypeByName(PublicationFileType.Name.OTHER),
                    formBean.getDataFile());
        } catch (IOException e) {
            LOG.error(e);
            return "zebrashare/new-submission.page";
        }

        LOG.warn(publication.getZdbID());


//        LOG.warn(formBean.getLabZdbId());
//        if (formBean.getEditors() != null) {
//            Arrays.stream(formBean.getEditors()).forEach(LOG::warn);
//        }
//        LOG.warn(formBean.getDataFile().getOriginalFilename());
//        if (formBean.getImageFiles() != null) {
//            Arrays.stream(formBean.getImageFiles())
//                    .filter(Objects::nonNull)
//                    .filter(f -> f.getContentType().startsWith("image/"))
//                    .forEach(f -> LOG.warn(f.getOriginalFilename()));
//        }
//        if (formBean.getCaptions() != null) {
//            Arrays.stream(formBean.getCaptions()).forEach(LOG::warn);
//        }

        return "redirect:/";
    }

}
