package org.zfin.marker.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zfin.infrastructure.DataAlias;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

@Controller
@RequestMapping("/data")
public class DataAliasRedirectController {

    @RequestMapping(value = "/alias/{zdbID}", method = RequestMethod.GET)
    public String getRedirectPathForAlias(@PathVariable String zdbID) {
        DataAlias alias = getInfrastructureRepository().getDataAliasByID(zdbID);
        return "redirect:/" + alias.getDataZdbID();
    }

}
