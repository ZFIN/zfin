package org.zfin.alliancegenome;

import org.alliancegenome.curation_api.model.entities.Allele;
import org.alliancegenome.curation_api.response.ObjectResponse;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
public interface AlleleRESTInterfaceAlliance {

    @GET
    @Path("/allele/{curie}")
    @Produces({MediaType.APPLICATION_JSON})
    ObjectResponse<Allele> getAllele(@PathParam("curie") String curie);

    @POST
    @Path("/allele")
    @Consumes({MediaType.APPLICATION_JSON})
    ObjectResponse<Allele> addAllele(@HeaderParam("Authorization") String auth, Allele allele);

    @PUT
    @Path("/allele")
    @Consumes({MediaType.APPLICATION_JSON})
    ObjectResponse<Allele> updateAllele(Allele allele);

}
