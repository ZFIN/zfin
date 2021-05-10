package org.zfin.gwt.curation.ui;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Options;
import org.springframework.web.bind.annotation.PathVariable;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.ExpressionExperimentDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.event.ZfinDispatcher;

import javax.ws.rs.*;
import java.util.List;

public interface ExpressionCurationService extends DirectRestService {


    @GET
    @Path("/action/curation/{publicationID}/experiments")
    @Options(dispatcher = ZfinDispatcher.class)
    List<ExperimentDTO> getExperiments(@PathParam("publicationID") String publicationID);

    @GET
    @Path("/action/curation/{publicationID}/expression-experiments")
    @Options(dispatcher = ZfinDispatcher.class)
    List<ExpressionExperimentDTO> getExpressionExperiments(@PathParam("publicationID") String publicationID);

    @POST
    @Path("/action/curation/{publicationID}/expression-experiments")
    @Options(dispatcher = ZfinDispatcher.class)
    ExpressionExperimentDTO createExpressionExperiment(@PathParam("publicationID") String publicationID, ExpressionExperimentDTO dto);


    @GET
    @Path("/action/curation/{publicationID}/{antibodyID}/genes")
    @Options(dispatcher = ZfinDispatcher.class)
    List<MarkerDTO> getGenesByAntibody(@PathParam("publicationID") String publicationID,
                                       @PathParam("antibodyID") String geneID) throws PublicationNotFoundException;

    @GET
    @Path("/action/curation/{publicationID}/{geneID}/genbank-accessions")
    @Options(dispatcher = ZfinDispatcher.class)
    public List<ExpressionExperimentDTO> getGenbankAccessions(@PathParam("publicationID") String publicationID,
                                                              @PathParam("geneID") String geneID);

    @GET
    @Path("/action/curation/{publicationID}/{geneID}/antibodies")
    @Options(dispatcher = ZfinDispatcher.class)
    public List<MarkerDTO> getAntibodiesByGene(@PathParam("publicationID") String publicationID,
                                               @PathParam("geneID") String geneID);

    @DELETE
    @Path("/action/curation/{publicationID}/expression-experiments/{expressionExperimentID}")
    @Options(dispatcher = ZfinDispatcher.class)
    public void deleteExperiment(@PathParam("publicationID") String publicationID,
                                 @PathParam("expressionExperimentID") String expressionExperimentID) ;


}
