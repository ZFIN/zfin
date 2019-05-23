package org.zfin.database.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.database.PostgresSession;
import org.zfin.database.repository.PostgresRepository;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Serves the summary of the database web page
 */
@Controller
@RequestMapping(value = "/database")
public class SysmasterController {

    /**
     * @return redirect to overview page.
     */
    @RequestMapping(value = "/overview")
    protected String overview() {
        return "dev-tools/database/overview.page";
    }

    @RequestMapping(value = "/database-summary")
    protected String summary(Model model,
                             @ModelAttribute("formBean")
                                     DatabaseFormBean formBean) {
        List<PostgresSession> databases = PostgresRepository.getSystemDatabases(formBean);
        model.addAttribute("databases", databases);
        return "dev-tools/database/summary.page";
    }

    @RequestMapping(value = "/all-sessions")
    protected String allSessions(Model model,
                                 @ModelAttribute("formBean")
                                         DatabaseFormBean formBean) {
        List<PostgresSession> sessions = PostgresRepository.getAllSessions(formBean);
        model.addAttribute("sessions", sessions);
        model.addAttribute("dbnameList", getDbNameList());
        model.addAttribute("formBean", formBean);
        return "dev-tools/database/all-sessions.page";
    }

    @RequestMapping(value = "/view-session/{ID}")
    protected String viewSession(Model model,
                                 @ModelAttribute("formBean")
                                         DatabaseFormBean formBean,
                                 @PathVariable("ID") String idString) {
        int id = Integer.valueOf(idString);
        //model.addAttribute("session", session);
        return "dev-tools/database/view-session.page";
    }

    public Map<String, String> getDbNameList() {
        LinkedHashMap<String, String> dateList = new LinkedHashMap<>();
        List<String> dbNames = PostgresRepository.getAllDbNames();
        Collections.sort(dbNames);
        for (String dbName : dbNames) {
            String name = dbName.trim();
            dateList.put(name, name);
        }
        return dateList;
    }


}
