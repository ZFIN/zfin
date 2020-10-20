package org.zfin.framework.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This Controller just takes the tiles view name and returns that ModelAndView object
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

    @RequestMapping("/gwt/transcript-edit")
    protected String showTranscriptEdit() throws Exception {
        return "dev-tools/gwt/transcript-edit";
    }

    @RequestMapping("/gwt/clone-edit")
    protected String showCloneEdit() throws Exception {
        return "dev-tools/gwt/clone-edit";
    }

    @RequestMapping("/gwt/go-edit")
    protected String showGoEdit() throws Exception {
        return "dev-tools/gwt/go-edit";
    }

    @RequestMapping("/gwt/antibody-edit")
    protected String showAntibodyEdit() throws Exception {
        return "dev-tools/gwt/antibody-edit";
    }

    @RequestMapping("/gwt/gene-edit")
    protected String showGeneEdit() throws Exception {
        return "dev-tools/gwt/gene-edit";
    }

    @RequestMapping("/gwt/alternate-gene-edit")
    protected String showAlternateGeneEdit() throws Exception {
        return "dev-tools/gwt/alternate-gene-edit";
    }

    @RequestMapping("/gwt/test-composite")
    protected String showTestComposite() throws Exception {
        return "dev-tools/gwt/test-composite";
    }

    @RequestMapping("/gwt/image-edit-test")
    protected String showimageEditTest() throws Exception {
        return "dev-tools/gwt/image-edit-test";
    }

}