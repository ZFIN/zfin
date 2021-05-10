package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationFile;
import org.zfin.publication.PublicationFileType;
import org.zfin.publication.repository.PublicationRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;

@Controller
@RequestMapping("/publication")
public class PublicationFileController {

    private static final Logger LOG = LogManager.getLogger(PublicationFileController.class);

    @Autowired
    private PublicationRepository publicationRepository;

    private JsonResultResponse<PublicationFile> getViewForPublication(Publication publication) {
        JsonResultResponse<PublicationFile> response = new JsonResultResponse<>();
        response.setResults(publication.getFiles());
        response.setTotal(publication.getFiles().size());
        response.addSupplementalData("fileTypes", publicationRepository.getAllPublicationFileTypes());
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/{id}/files", method = RequestMethod.GET)
    @JsonView(View.Default.class)
    public JsonResultResponse<PublicationFile> getPublicationFiles(@PathVariable String id,
                                                                   HttpServletRequest request) {
        Publication publication = publicationRepository.getPublication(id);
        JsonResultResponse<PublicationFile> response = getViewForPublication(publication);
        response.setHttpServletRequest(request);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/{id}/files", method = RequestMethod.POST)
    @JsonView(View.Default.class)
    public JsonResultResponse<PublicationFile> addPublicationFile(@PathVariable String id,
                                                                  @RequestParam int fileType,
                                                                  @RequestParam MultipartFile file,
                                                                  HttpServletRequest request) {
        Publication publication = publicationRepository.getPublication(id);
        PublicationFileType type = publicationRepository.getPublicationFileType(fileType);

        try {
            publicationRepository.addPublicationFile(publication, type, file);
        } catch (IOException e) {
            LOG.error("Error processing pub file", e);
            throw new InvalidWebRequestException("Error processing file");
        }

        // return the whole list because we might have replaced the original article, and to keep the sorting right
        JsonResultResponse<PublicationFile> response = getViewForPublication(publication);
        response.setHttpServletRequest(request);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/files/{id}", method = RequestMethod.POST)
    @JsonView(View.Default.class)
    public JsonResultResponse<PublicationFile> editPublicationFile(@PathVariable long id,
                                      @RequestBody PublicationFile updated,
                                      HttpServletRequest request) {
        PublicationFile pubFile = publicationRepository.getPublicationFile(id);
        Publication publication = pubFile.getPublication();

        pubFile.setType(publicationRepository.getPublicationFileType(updated.getType().getId()));

        HibernateUtil.createTransaction();
        HibernateUtil.currentSession().update(pubFile);
        HibernateUtil.flushAndCommitCurrentSession();

        // return the whole list because we might have replaced the original article, and to keep the sorting right
        JsonResultResponse<PublicationFile> response = getViewForPublication(publication);
        response.setHttpServletRequest(request);
        return response;
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
