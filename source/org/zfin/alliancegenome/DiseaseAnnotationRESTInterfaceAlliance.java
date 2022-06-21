package org.zfin.alliancegenome;

import org.alliancegenome.curation_api.model.entities.DiseaseAnnotation;
import org.alliancegenome.curation_api.response.ObjectResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/api")
public interface DiseaseAnnotationRESTInterfaceAlliance {

    @GET
    @Path("/agm-disease-annotation/{curie}")
    @Produces({MediaType.APPLICATION_JSON})
    ObjectResponse<DiseaseAnnotation> getDiseaseAnnotation(@HeaderParam("Authorization") String auth,
                                                           @PathParam("curie") String curie
                                                           );

    @POST
    @Path("/agm-disease-annotation")
    @Consumes({MediaType.APPLICATION_JSON})
    ObjectResponse<DiseaseAnnotation> addDiseaseAnnotation(@HeaderParam("Authorization") String auth,
                                                           DiseaseAnnotation annotation);

    @PUT
    @Path("/agm-disease-annotation")
    @Consumes({MediaType.APPLICATION_JSON})
    ObjectResponse<DiseaseAnnotation> updateDiseaseAnnotation(DiseaseAnnotation annotation);

}
