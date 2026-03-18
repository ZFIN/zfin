package org.zfin.sequence.presentation;

import org.apache.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.sequence.DBLink;

import jakarta.servlet.http.HttpServletResponse;

import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

@Controller
@RequestMapping("/sequence")
public class DBLinkViewController {

    @RequestMapping(value = "/dblink/view/{zdbID}", method = RequestMethod.GET)
    public String viewDBLink(@PathVariable String zdbID, Model model, HttpServletResponse response) {
        DBLink dbLink = getSequenceRepository().getDBLinkByID(zdbID);
        if (dbLink == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }
        model.addAttribute("dbLink", dbLink);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "DBLink: " + dbLink.getAccessionNumber());
        return "sequence/dblink-view";
    }
}
