package org.zfin.publication.presentation;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationFile;
import org.zfin.publication.PublicationFileType;
import org.zfin.publication.repository.PublicationRepository;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/publication")
public class PublicationFileController {

    private static final Logger LOG = Logger.getLogger(PublicationFileController.class);

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private PublicationService publicationService;

    @ResponseBody
    @RequestMapping(value = "/{id}/files", method = RequestMethod.GET)
    public Collection<PublicationFilePresentationBean> getPublicationFiles(@PathVariable String id) {
        Publication publication = publicationRepository.getPublication(id);
        return publication.getFiles().stream()
                .map(publicationService::convertToPublicationFilePresentationBean)
                .collect(Collectors.toList());
    }

    @ResponseBody
    @RequestMapping(value = "/{id}/files", method = RequestMethod.POST)
    public PublicationFilePresentationBean addPublicationFile(@PathVariable String id,
                                                              @RequestParam int fileType,
                                                              @RequestParam MultipartFile file) {
        Publication publication = publicationRepository.getPublication(id);
        PublicationFileType type = publicationRepository.getPublicationFileType(fileType);
        PublicationFile pubFile;
        try {
            pubFile = publicationService.processPublicationFile(
                    publication, file.getOriginalFilename(), type, file.getInputStream());
        } catch (IOException e) {
            LOG.error("Error processing pub file", e);
            throw new InvalidWebRequestException("Error processing file");
        }

        Transaction tx = HibernateUtil.createTransaction();
        HibernateUtil.currentSession().save(pubFile);
        tx.commit();

        return publicationService.convertToPublicationFilePresentationBean(pubFile);
    }

    @ResponseBody
    @RequestMapping(value = "/files/{id}", method = RequestMethod.DELETE)
    public String deletePublicationFile(@PathVariable long id) {
        PublicationFile pubFile = publicationRepository.getPublicationFile(id);

        Transaction tx = HibernateUtil.createTransaction();
        HibernateUtil.currentSession().delete(pubFile);
        tx.commit();

        return "OK";
    }

    @ResponseBody
    @RequestMapping(value = "/file-types", method = RequestMethod.GET)
    public Collection<PublicationFileType> getAllFileTypes() {
        return publicationRepository.getAllPublicationFileTypes();
    }

}
