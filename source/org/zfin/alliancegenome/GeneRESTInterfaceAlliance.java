package org.zfin.alliancegenome;

import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.model.entities.Gene;
import org.alliancegenome.curation_api.response.ObjectResponse;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
public interface GeneRESTInterfaceAlliance {

    @GET
    @Path("/gene/{curie}")
    @Produces({MediaType.APPLICATION_JSON})
    ObjectResponse<Gene> getAffectedGenomicModel(@PathParam("curie") String curie);

    @POST
    @Path("/gene")
    @Consumes({MediaType.APPLICATION_JSON})
    ObjectResponse<Gene> addAffectedGenomicModel(@HeaderParam("Authorization") String auth,
                                                 Gene model);

    @PUT
    @Path("/gene")
    @Consumes({MediaType.APPLICATION_JSON})
    ObjectResponse<Gene> updateAffectedGenomicModel(Gene model);

}
