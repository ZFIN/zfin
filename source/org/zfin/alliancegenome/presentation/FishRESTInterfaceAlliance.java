package org.zfin.alliancegenome.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.model.entities.Gene;
import org.alliancegenome.curation_api.model.entities.orthology.GeneToGeneOrthologyGenerated;
import org.alliancegenome.curation_api.response.ObjectResponse;
import org.alliancegenome.curation_api.response.SearchResponse;
import org.alliancegenome.curation_api.view.View;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;

@Path("/api")
@Produces({"application/json"})
@Consumes({"application/json"})
public interface FishRESTInterfaceAlliance {

    @POST
    @Path("/agm/search")
    @JsonView({View.ForPublic.class})
    SearchResponse<AffectedGenomicModel> search(@HeaderParam("Authorization") String auth,@DefaultValue("0") @QueryParam("page") Integer page, @DefaultValue("10") @QueryParam("limit") Integer limit, @RequestBody HashMap<String, Object> params);

}
