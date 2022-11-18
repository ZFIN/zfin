package org.zfin.framework.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This Controller just takes the view name and returns that ModelAndView object
 * in case you do not have any logic to perform in the Controller other than
 * passing through to the view handler.
 */
@Controller
@RequestMapping("/devtool")
public class SimplePassThroughController {

    @RequestMapping("/gwt/lookup")
    protected String showLookupTestPage() throws Exception {
        return "dev-tools/gwt/lookup";
    }

    @RequestMapping("/gwt/lookup-table")
    protected String showLookupTableTestPage() throws Exception {
        return "dev-tools/gwt/lookup-table";
    }

    @RequestMapping("/gwt/curation-base")
    protected String showCurationBase() throws Exception {
        return "dev-tools/gwt/curation-base";
    }

    @RequestMapping("/gwt/image-edit-test")
    protected String showimageEditTest() throws Exception {
        return "dev-tools/gwt/image-edit-test";
    }

}