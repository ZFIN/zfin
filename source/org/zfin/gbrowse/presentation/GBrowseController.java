package org.zfin.gbrowse.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;


@Controller
public class GBrowseController {

    private @Autowired HttpServletRequest request;

    @RequestMapping("/gbrowse")
    public String gbrowse(Model model) {
        model.addAttribute("requestParams", request.getQueryString());
        return "gbrowse/gbrowse-view.page";
    }

}
