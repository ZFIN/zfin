package org.zfin.properties.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ZfinPropertiesController {

    @RequestMapping(value = "/zfin-properties")
    protected String showZfinProperties(Model model) throws Exception {
        return "dev-tools/zfin-properties.page" ;
    }
}
