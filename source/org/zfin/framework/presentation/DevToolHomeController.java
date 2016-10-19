package org.zfin.framework.presentation;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Simple controller that serves the developer tools home page.
 */
@org.springframework.stereotype.Controller
public class DevToolHomeController {

    @RequestMapping("devtool/home")
    public String homePage() throws Exception {
        return "dev-tools/home.page";
    }

}
