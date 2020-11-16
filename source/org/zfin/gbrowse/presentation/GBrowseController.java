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
        model.addAttribute("urlPrefix","/gb2/gbrowse/zfin_ensembl_GRCz11");
        return "gbrowse/gbrowse-view";
    }

    @RequestMapping("/gbrowse/GRCz10")
    public String gbrowseGRCz10(Model model) {
        model.addAttribute("requestParams", request.getQueryString());
        model.addAttribute("urlPrefix","/gb2/gbrowse/zfin_ensembl");
        return "gbrowse/gbrowse-view";
    }


    @RequestMapping("/gbrowse/Zv9")
    public String gbrowseZv9(Model model) {
        model.addAttribute("requestParams", request.getQueryString());
        model.addAttribute("urlPrefix","/gb2/gbrowse/zfin_ensembl_Zv9");
        return "gbrowse/gbrowse-view";
    }
}
