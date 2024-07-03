package org.zfin.textpresso;

import org.springframework.web.bind.annotation.RequestBody;
import org.zfin.search.IndexerStatus;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;

@Path("/textpresso/zfin/v1/textpresso/api/get_category_matches_document_fulltext")
public interface TextpressoRESTInterface {

    @POST
    @Path("")
    @Produces({MediaType.APPLICATION_JSON})
    MatchedSentences getResponse(@RequestBody HashMap<String, Object> params);

}
