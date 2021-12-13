package org.zfin.alliancegenome;

import org.alliancegenome.curation_api.model.entities.DiseaseAnnotation;
import org.alliancegenome.curation_api.response.ObjectResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api")
public interface DiseaseAnnotationRESTInterfaceAlliance {

    @GET
    @Path("/disease-annotation/{curie}")
    @Produces({MediaType.APPLICATION_JSON})
    ObjectResponse<DiseaseAnnotation> getDiseaseAnnotation(@PathParam("curie") String curie);

    @POST
    @Path("/disease-annotation")
    @Consumes({MediaType.APPLICATION_JSON})
    ObjectResponse<DiseaseAnnotation> addDiseaseAnnotation(DiseaseAnnotation annotation);

    @PUT
    @Path("/disease-annotation")
    @Consumes({MediaType.APPLICATION_JSON})
    ObjectResponse<DiseaseAnnotation> updateDiseaseAnnotation(DiseaseAnnotation annotation);

}
