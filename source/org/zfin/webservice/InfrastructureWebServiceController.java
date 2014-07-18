package org.zfin.webservice;

import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zfin.database.UnloadInfo;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;
import org.zfin.webservice.schema.Anatomy;
import org.zfin.webservice.schema.Gene;
import org.zfin.webservice.schema.GeneSearchResponse;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Comes in via /webservice as defined by web.xml.
 */
@Controller
public class InfrastructureWebServiceController extends AbstractMarkerWebService {


    @RequestMapping(value = "databaseInfo", method = RequestMethod.GET)
    public
    @ResponseBody
    String getDatabaseInfo(HttpServletResponse response) {
        UnloadInfo unloadInfo = getInfrastructureRepository().getUnloadInfo();
        Gson json = new Gson();
        String release = json.toJson(unloadInfo);
        response.setContentType("application/json");

        return release;
    }


}
