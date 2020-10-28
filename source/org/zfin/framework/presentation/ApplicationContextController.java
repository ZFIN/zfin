package org.zfin.framework.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.presentation.ExpressionResultDisplay;
import org.zfin.expression.presentation.ExpressionResultFormBean;
import org.zfin.ontology.GenericTerm;

import javax.servlet.http.HttpServletRequest;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

/**
 * Controller that obtains the meta data for the database.
 */
@Controller
@RequestMapping(value = "/devtool")
public class ApplicationContextController {

    @Autowired
    private HttpServletRequest request;

    @RequestMapping("/application-context")
    protected String showApplicationContext(@ModelAttribute("formBean") ApplicationContextBean form,
                                            Model model)
            throws Exception {

        WebApplicationContext context = RequestContextUtils.getWebApplicationContext(request);
        form.setApplicationContext(context);

        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        model.addAttribute("runtimeMXBean", runtime);
        Field jvm = runtime.getClass().getDeclaredField("jvm");
        jvm.setAccessible(true);
        return "dev-tools/application-context";
    }

    @RequestMapping("/fx-stage-range-violations")
    protected String showStageRangeViolations(@ModelAttribute("formBean") ExpressionResultFormBean form,
                                              Model model)
            throws Exception {
        List<ExpressionResult> expressionResultsViolateStageRanges = getOntologyRepository().getExpressionResultsViolateStageRanges();
        Map<String, ExpressionResultDisplay> displaySet = new HashMap<>();
        for (ExpressionResult result : expressionResultsViolateStageRanges) {
            ExpressionResultDisplay display = new ExpressionResultDisplay(result);
            String key = display.getUniqueKey();
            ExpressionResultDisplay existingDisplay = displaySet.get(key);
            if (existingDisplay == null)
                displaySet.put(key, display);
            else
                existingDisplay.addExpressionResult(result);
        }
        if (displaySet.size() > 0) {
            ExpressionResultDisplay next = displaySet.values().iterator().next();
            form.setStartStageOboIDOne(next.getStart().getZdbID());
            form.setEndStageOboIDOne(next.getEnd().getZdbID());
            form.setStartStageOboIDTwo(next.getStart().getZdbID());
            form.setEndStageOboIDTwo(next.getEnd().getZdbID());
        }
        model.addAttribute("expressionResultDisplays", displaySet.values());
        model.addAttribute("violations", expressionResultsViolateStageRanges);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "dev-tools/fx-stage-range-violations";
    }

    @RequestMapping("/merged-terms-used-in-relationships")
    protected String showMergedTermsInRelationship(@ModelAttribute("formBean") ExpressionResultFormBean form,
                                                   Model model)
            throws Exception {
        List<GenericTerm> expressionResultsViolateStageRanges = getOntologyRepository().getMergedTermsInTermRelationships();
        model.addAttribute("mergeTerms", expressionResultsViolateStageRanges);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "dev-tools/merged-terms-used-in-relationships";
    }

    @RequestMapping("/terms-without-relationships")
    protected String showTermsWithoutRelationships(@ModelAttribute("formBean") ExpressionResultFormBean form,
                                                   Model model)
            throws Exception {
        List<GenericTerm> expressionResultsViolateStageRanges = getOntologyRepository().getActiveTermsWithoutRelationships();
        model.addAttribute("activeTerms", expressionResultsViolateStageRanges);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "dev-tools/terms-without-relationships";
    }


    @RequestMapping("/fx-stage-range-update")
    protected String editStageRangeViolations(@ModelAttribute("formBean") ExpressionResultFormBean form,
                                              Model model)
            throws Exception {
        List<ExpressionResult> expressionResultsViolateStageRanges = getOntologyRepository().getExpressionResultsViolateStageRanges();
        Map<String, ExpressionResultDisplay> displaySet = new HashMap<>();
        for (ExpressionResult result : expressionResultsViolateStageRanges) {
            ExpressionResultDisplay display = new ExpressionResultDisplay(result);
            String key = display.getUniqueKey();
            ExpressionResultDisplay existingDisplay = displaySet.get(key);
            if (existingDisplay == null)
                displaySet.put(key, display);
            else
                existingDisplay.addExpressionResult(result);
        }
        ExpressionResultDisplay next = displaySet.values().iterator().next();
        form.setStartStageOboIDOne(next.getStart().getZdbID());
        form.setEndStageOboIDOne(next.getEnd().getZdbID());
        form.setStartStageOboIDTwo(next.getStart().getZdbID());
        form.setEndStageOboIDTwo(next.getEnd().getZdbID());
        model.addAttribute("expressionResultDisplays", displaySet.values());
        model.addAttribute("violations", expressionResultsViolateStageRanges);
        model.addAttribute(LookupStrings.FORM_BEAN, form);
        return "dev-tools/fx-stage-range-violations";
    }


}

