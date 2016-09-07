package org.zfin.framework.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
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
        return "lookup";
    }

    @RequestMapping("/gwt/lookup-table")
    protected String showLookupTableTestPage() throws Exception {
        return "lookup-table";
    }

    @RequestMapping("/gwt/curation-base")
    protected String showCurationBase() throws Exception {
        return "curation-base";
    }

    @RequestMapping("/gwt/transcript-edit")
    protected String showTranscriptEdit() throws Exception {
        return "transcript-edit";
    }

    @RequestMapping("/gwt/clone-edit")
    protected String showCloneEdit() throws Exception {
        return "clone-edit";
    }

    @RequestMapping("/gwt/go-edit")
    protected String showGoEdit() throws Exception {
        return "go-edit";
    }

    @RequestMapping("/gwt/antibody-edit")
    protected String showAntibodyEdit() throws Exception {
        return "antibody-edit";
    }

    @RequestMapping("/gwt/gene-edit")
    protected String showGeneEdit() throws Exception {
        return "gene-edit";
    }

    @RequestMapping("/gwt/alternate-gene-edit")
    protected String showAlternateGeneEdit() throws Exception {
        return "alternate-gene-edit";
    }

    @RequestMapping("/gwt/test-composite")
    protected String showTestComposite() throws Exception {
        return "test-composite";
    }

    @RequestMapping("/gwt/image-edit-test")
    protected String showimageEditTest() throws Exception {
        return "image-edit-test";
    }
    @RequestMapping("/gwt/image-edit")
    protected String showimageEdit() throws Exception {
        return "image-edit";
    }

    @RequestMapping("/http-redirect")
    protected String showLookupTestPage(@ModelAttribute("formBean") ApgPaginationBean bean) throws Exception {
        return "home-page-redirect.page";
    }

}