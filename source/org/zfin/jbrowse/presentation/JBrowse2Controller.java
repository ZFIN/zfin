package org.zfin.jbrowse.presentation;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@Log4j2
public class JBrowse2Controller {

    private @Autowired HttpServletRequest request;

    @RequestMapping("/jbrowse2")
    public String jbrowse2(Model model) {
        return "jbrowse2/jbrowse2-view";
    }

}
