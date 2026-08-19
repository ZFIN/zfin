package org.zfin.infrastructure.presentation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.infrastructure.submission.SubmissionLogService;
import org.zfin.infrastructure.submission.SubmissionOutcome;

/**
 * Read only review of the public form submissions recorded in submission_log, including the ones
 * that were silently discarded. Lives under /devtool so it inherits the root only access rule in
 * security.xml.
 */
@Controller
@RequestMapping("/devtool/submissions")
public class SubmissionLogController {

    /**
     * Enough to cover a spam wave at a glance. Reaching further back is a matter of raising the
     * limit, so there is nothing this page cannot show.
     */
    private static final int DEFAULT_LIMIT = 200;

    /**
     * A ceiling rather than paging: the table has to stay renderable, and a request for more rows
     * than this is really a request for SQL.
     */
    private static final int MAX_LIMIT = 5000;

    @GetMapping
    public String list(@RequestParam(value = "outcome", required = false) String outcome,
                       @RequestParam(value = "limit", defaultValue = "" + DEFAULT_LIMIT) int limit,
                       Model model) {
        SubmissionOutcome selectedOutcome = parseOutcome(outcome);
        int rows = Math.min(Math.max(limit, 1), MAX_LIMIT);
        model.addAttribute("submissions", SubmissionLogService.getRecent(selectedOutcome, rows));
        model.addAttribute("total", SubmissionLogService.count(selectedOutcome));
        model.addAttribute("outcomes", SubmissionOutcome.values());
        model.addAttribute("selectedOutcome", selectedOutcome);
        model.addAttribute("limit", rows);
        model.addAttribute("maxLimit", MAX_LIMIT);
        return "dev-tools/submissions";
    }

    /**
     * An unrecognized outcome shows everything rather than an error: this only ever arrives from
     * the filter dropdown or a hand edited URL.
     */
    private SubmissionOutcome parseOutcome(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return SubmissionOutcome.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
