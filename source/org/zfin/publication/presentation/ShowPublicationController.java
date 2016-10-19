package org.zfin.publication.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.ActiveData;
import org.zfin.publication.Publication;

import java.util.List;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

@Controller
@RequestMapping(value = "/publication")
public class ShowPublicationController {

    @RequestMapping("/list/{zdbID}")
    public String fishCitationList(@PathVariable(value = "zdbID") String zdbID,
                                   @RequestParam(value = "orderBy", required = false) String orderBy,
                                   Model model) throws Exception {

        List<Publication> list = getPublicationRepository().getPubsForDisplay(zdbID);
        if (list == null)
            return LookupStrings.idNotFound(model, zdbID);
        ShowPublicationBean bean = new ShowPublicationBean(list);
        bean.setEntityID(zdbID);
        bean.setEntity(ActiveData.getType(zdbID).getEntity(zdbID));
        if (StringUtils.isNotEmpty(orderBy))
            bean.setOrderBy(orderBy);
        model.addAttribute("formBean", bean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "publication list");
        return "publication/publication-list.page";
    }


}
