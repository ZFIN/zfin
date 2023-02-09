package org.zfin.jbrowse.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;


@Controller
public class JBrowseController {

    private @Autowired HttpServletRequest request;

    @RequestMapping("/jbrowse")
    public String jbrowse(Model model) {
        model.addAttribute("requestParams", request.getQueryString());
        model.addAttribute("urlPrefix",JBrowseImage.calculateBaseUrl());
        return "jbrowse/jbrowse-view";
    }

}
