package org.zfin.sequence.load;

import org.springframework.web.bind.annotation.RequestParam;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/lookup")
public interface EnsemblTranscriptRESTInterface {

    @GET
    @Path("/id/{ensemblID}")
    @Produces({MediaType.APPLICATION_JSON})
    EnsemblTranscript getTranscriptInfo(@PathParam("ensemblID") String site,
                                        @RequestParam("content-type") String contentType);

}
