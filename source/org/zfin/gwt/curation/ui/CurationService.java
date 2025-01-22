package org.zfin.gwt.curation.ui;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Options;
import org.zfin.gwt.root.dto.ExpressionExperimentDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.event.ZfinDispatcher;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.List;

/**
 * General access web services (mostly by publication).
 */
public interface CurationService extends DirectRestService {

    @GET
    @Path("/action/curation/{publicationID}/genes")
    @Options(dispatcher = ZfinDispatcher.class)
    public List<MarkerDTO> getGenes(@PathParam("publicationID") String pubID) throws PublicationNotFoundException;

    @GET
    @Path("/action/curation/{publicationID}/antibodies")
    @Options(dispatcher = ZfinDispatcher.class)
    public List<MarkerDTO>  getAntibodies(@PathParam("publicationID") String publicationID);

    @GET
    @Path("/action/curation/assays")
    @Options(dispatcher = ZfinDispatcher.class)
    public List<String> getAssays();
}
