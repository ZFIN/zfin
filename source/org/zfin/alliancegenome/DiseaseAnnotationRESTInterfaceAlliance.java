package org.zfin.alliancegenome;

import org.alliancegenome.curation_api.exceptions.ApiErrorException;
import org.alliancegenome.curation_api.exceptions.ObjectUpdateException;
import org.alliancegenome.curation_api.model.entities.AGMDiseaseAnnotation;
import org.alliancegenome.curation_api.model.entities.DiseaseAnnotation;
import org.alliancegenome.curation_api.model.ingest.dto.AGMDiseaseAnnotationDTO;
import org.alliancegenome.curation_api.response.ObjectResponse;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;

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
    ObjectResponse<AGMDiseaseAnnotation> addDiseaseAnnotation(@HeaderParam("Authorization") String auth,
                                                              AGMDiseaseAnnotation annotation);

    @POST
    @Path("/agm-disease-annotation/upsert")
    @Consumes({MediaType.APPLICATION_JSON})
    AGMDiseaseAnnotation createZfinAgmDiseaseAnnotations(@HeaderParam("Authorization") String auth,
                                                                         AGMDiseaseAnnotationDTO annotationData) throws IOException, ApiException;

    @PUT
    @Path("/agm-disease-annotation")
    @Consumes({MediaType.APPLICATION_JSON})
    ObjectResponse<DiseaseAnnotation> updateDiseaseAnnotation(DiseaseAnnotation annotation);

}
