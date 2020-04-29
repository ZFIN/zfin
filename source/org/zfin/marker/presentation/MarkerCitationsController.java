package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationService;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Log4j2
public class MarkerCitationsController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private PublicationService publicationService;

    @JsonView(View.CitationsAPI.class)
    @RequestMapping("/marker/{zdbID}/citations")
    public JsonResultResponse<Publication> getMarkerCitations(@PathVariable String zdbID,
                                                              @Version Pagination pagination) throws IOException, SolrServerException {

        JsonResultResponse<Publication> response = publicationService.getCitationsByXref(zdbID, pagination);
        response.setHttpServletRequest(request);

        return response;
    }

    @RequestMapping("/marker/{zdbID}/citations.tsv")
    public void downloadMarkerCitations(@PathVariable String zdbID,
                                        HttpServletResponse response) throws IOException, SolrServerException {
        response.setContentType(MediaType.TEXT_PLAIN);
        String attachmentName = zdbID + "_citations_" + DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s.tsv\"", attachmentName));
        PrintWriter writer = response.getWriter();
        writer.println(String.join("\t",
                "ZFIN Publication ID",
                "PubMed ID",
                "Authors",
                "Title",
                "Journal",
                "Year",
                "Volume",
                "Pages"
        ));
        publicationService.getAllCitationsByXref(zdbID, (publications) -> {
            publications.forEach(pub -> {
                Object[] row = new Object[]{
                        pub.getZdbID(),
                        pub.getAccessionNumber(),
                        pub.getAuthors(),
                        pub.getTitle(),
                        pub.getJournal().getName(),
                        pub.getYear(),
                        pub.getVolume(),
                        pub.getPages()
                };
                writer.println(Arrays.stream(row)
                        .map(col -> Objects.toString(col, ""))
                        .collect(Collectors.joining("\t"))
                );
            });
        });
    }
}
