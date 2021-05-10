package org.zfin.publication.presentation;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zfin.curation.service.CurationDTOConversionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationProcessingChecklistEntry;
import org.zfin.publication.repository.PublicationRepository;

import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/publication")
public class PublicationProcessingChecklistController {

    @Autowired
    private CurationDTOConversionService dtoConversionService;

    @Autowired
    private PublicationRepository publicationRepository;

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/checklist", method = RequestMethod.GET)
    private Collection<ProcessingTaskBean> getProcessingChecklist(@PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        return publication.getProcessingChecklistEntries()
                .stream()
                .map(dtoConversionService::toProcessingTaskBean)
                .collect(Collectors.toList());
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/checklist", method = RequestMethod.POST)
    private ProcessingTaskBean addProcessingChecklistEntry(@PathVariable String zdbID,
                                                           @RequestBody ProcessingTaskBean task) {
        Publication publication = publicationRepository.getPublication(zdbID);

        PublicationProcessingChecklistEntry entry = new PublicationProcessingChecklistEntry();
        entry.setPublication(publication);
        entry.setDate(new GregorianCalendar());
        entry.setPerson(ProfileService.getCurrentSecurityUser());
        entry.setTask(publicationRepository.getProcessingChecklistTask(task.getTask()));

        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        session.save(entry);
        tx.commit();

        return dtoConversionService.toProcessingTaskBean(entry);
    }

    @ResponseBody
    @RequestMapping(value = "/checklist/{entryID}", method = RequestMethod.DELETE)
    private void deleteProcessingChecklistEntry(@PathVariable long entryID) {
        PublicationProcessingChecklistEntry entry = publicationRepository.getProcessingChecklistEntry(entryID);

        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        session.delete(entry);
        tx.commit();
    }
}
