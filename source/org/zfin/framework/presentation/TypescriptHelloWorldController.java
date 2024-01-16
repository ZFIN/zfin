package org.zfin.framework.presentation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class TypescriptHelloWorldController {

    @RequestMapping("/devtool/typescript-hello-world")
    public ModelAndView homePage() {
        return new ModelAndView("dev-tools/typescript-hello-world");
    }

}
