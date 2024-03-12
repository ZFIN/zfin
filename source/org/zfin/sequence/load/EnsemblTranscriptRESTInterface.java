package org.zfin.sequence.load;

import org.springframework.web.bind.annotation.RequestParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/lookup")
public interface EnsemblTranscriptRESTInterface {

    @GET
    @Path("/id/{ensemblID}")
    @Produces({MediaType.APPLICATION_JSON})
    EnsemblTranscript getTranscriptInfo(@PathParam("ensemblID") String site,
                                        @RequestParam("content-type") String contentType);

}
