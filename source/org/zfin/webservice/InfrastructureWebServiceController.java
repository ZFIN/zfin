package org.zfin.webservice;

import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.database.UnloadInfo;
import org.zfin.properties.ZfinPropertiesEnum;

import javax.servlet.http.HttpServletResponse;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Comes in via /webservice as defined by web.xml.
 */
@Controller
public class InfrastructureWebServiceController {


    @RequestMapping(value = "databaseInfo", method = RequestMethod.GET)
    public
    @ResponseBody
    String getDatabaseInfo(HttpServletResponse response) {
        UnloadInfo unloadInfo = getInfrastructureRepository().getUnloadInfo();
        unloadInfo.setDatabaseName(ZfinPropertiesEnum.DB_NAME.value());
        Gson json = new Gson();
        String release = json.toJson(unloadInfo);
        response.setContentType("application/json");

        return release;
    }


}
