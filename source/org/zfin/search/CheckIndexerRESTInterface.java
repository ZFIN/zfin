package org.zfin.search;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/solr")
public interface CheckIndexerRESTInterface {

    @GET
    @Path("/{site}/dataimport")
    @Produces({MediaType.APPLICATION_JSON})
    IndexerStatus getIndexerStatus(@PathParam("site") String site);

}
