package org.zfin.search;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/solr")
public interface CheckIndexerRESTInterface {

    @GET
    @Path("/{site}/dataimport")
    @Produces({MediaType.APPLICATION_JSON})
    IndexerStatus getIndexerStatus(@PathParam("site") String site);

}
