package org.zfin.alliancegenome.presentation;

import jakarta.ws.rs.*;
import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.response.SearchResponse;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import java.util.HashMap;

@Path("/indexing_priority")
@Produces({"application/json"})
@Consumes({"application/json"})
public interface PriorityRESTInterface {

    @GET
    @Path("/get_priority_tag/{pubID}")
    PriorityTag search(@HeaderParam("Authorization") String auth, @PathParam("pubID") String pubID);

}
