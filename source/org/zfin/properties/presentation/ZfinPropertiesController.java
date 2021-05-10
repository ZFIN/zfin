package org.zfin.properties.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/devtool")
public class ZfinPropertiesController {

    @RequestMapping(value = "/zfin-properties")
    protected String showZfinProperties() throws Exception {
        return "dev-tools/zfin-properties";
    }
}
