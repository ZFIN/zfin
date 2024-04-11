package org.zfin.alliancegenome;

import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.model.entities.DiseaseAnnotation;
import org.alliancegenome.curation_api.response.ObjectResponse;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
public interface AgmRESTInterfaceAlliance {

    @GET
    @Path("/agm/{curie}")
    @Produces({MediaType.APPLICATION_JSON})
    ObjectResponse<AffectedGenomicModel> getAffectedGenomicModel(@PathParam("curie") String curie);

    @POST
    @Path("/agm")
    @Consumes({MediaType.APPLICATION_JSON})
    ObjectResponse<AffectedGenomicModel> addAffectedGenomicModel(@HeaderParam("Authorization") String auth,
                                                                 AffectedGenomicModel model);

    @PUT
    @Path("/agm")
    @Consumes({MediaType.APPLICATION_JSON})
    ObjectResponse<AffectedGenomicModel> updateAffectedGenomicModel(AffectedGenomicModel model);

}
