package org.zfin.zirc.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.zirc.entity.LineSubmission;
import org.zfin.zirc.entity.LineSubmissionPerson;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/zirc")
public class ZircDashboardController {

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String viewDashboard(Model model) {
        List<LineSubmission> submissions = HibernateUtil.currentSession()
                .createQuery("from ZircLineSubmission order by createdAt desc", LineSubmission.class)
                .list();
        // No status column on LineSubmission yet, so everything lands in "Active".
        model.addAttribute("activeSubmissions", submissions);
        model.addAttribute("closedSubmissions", Collections.emptyList());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Line Submission Dashboard");
        return "zirc/dashboard";
    }

    @RequestMapping(value = "/line-submission/{zdbID}", method = RequestMethod.GET)
    public String viewLineSubmission(@PathVariable String zdbID, Model model) {
        LineSubmission submission = HibernateUtil.currentSession().get(LineSubmission.class, zdbID);
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Line submission " + zdbID + " not found");
        }
        model.addAttribute("submission", submission);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Line Submission: " + submission.getName());
        return "zirc/line-submission-detail";
    }

    /**
     * Create-on-render: persists an empty LineSubmission so the ZdbIdGenerator mints a
     * fresh ZDB ID, then redirects to the edit page. Visiting and abandoning the edit
     * page leaves an unnamed stub row in the DB by design.
     *
     * The currently logged-in user is automatically linked as a "submitter" so the new
     * row already has a person association; if no one is logged in we skip the link.
     */
    @RequestMapping(value = "/line-submission/new", method = RequestMethod.GET)
    public String newLineSubmission() {
        LineSubmission submission = new LineSubmission();
        HibernateUtil.createTransaction();
        HibernateUtil.currentSession().persist(submission);

        Person currentUser = ProfileService.getCurrentSecurityUser();
        if (currentUser != null && currentUser.getZdbID() != null) {
            // Re-attach: getCurrentSecurityUser() returns a Person from the security context
            // that's detached from this session, which would trip the @Id @ManyToOne cascade.
            Person attachedUser = HibernateUtil.currentSession().getReference(Person.class, currentUser.getZdbID());
            LineSubmissionPerson lsp = new LineSubmissionPerson();
            lsp.setLineSubmission(submission);
            lsp.setPerson(attachedUser);
            lsp.setRole("submitter");
            lsp.setSortOrder(1);
            HibernateUtil.currentSession().persist(lsp);
        }

        HibernateUtil.flushAndCommitCurrentSession();
        return "redirect:/action/zirc/line-submission/" + submission.getZdbID() + "/edit";
    }

    @RequestMapping(value = "/line-submission/{zdbID}/edit", method = RequestMethod.GET)
    public String editLineSubmission(@PathVariable String zdbID, Model model) {
        LineSubmission submission = HibernateUtil.currentSession().get(LineSubmission.class, zdbID);
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Line submission " + zdbID + " not found");
        }
        model.addAttribute("submission", submission);
        String label = (submission.getName() != null && !submission.getName().isBlank())
                ? submission.getName() : zdbID;
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit Line Submission: " + label);
        return "zirc/line-submission-edit";
    }

    /**
     * Per-field save endpoint for the edit page. The {@code field} parameter is the entity
     * property name; mapped through an explicit switch so we never accept arbitrary column
     * updates. Empty strings collapse to null so the DB sees real NULLs.
     */
    @RequestMapping(value = "/line-submission/{zdbID}/update-field", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> updateField(@PathVariable String zdbID,
                                           @RequestParam("field") String field,
                                           @RequestParam(value = "value", required = false) String value) {
        LineSubmission submission = HibernateUtil.currentSession().get(LineSubmission.class, zdbID);
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Line submission " + zdbID + " not found");
        }

        String s = (value != null && !value.isBlank()) ? value.trim() : null;

        HibernateUtil.createTransaction();
        switch (field) {
            case "name":                       submission.setName(s); break;
            case "abbreviation":               submission.setAbbreviation(s); break;
            case "previousNames":              submission.setPreviousNames(s); break;
            case "featuresLinked":             submission.setFeaturesLinked(parseTriBool(s)); break;
            case "maternalBackground":         submission.setMaternalBackground(s); break;
            case "paternalBackground":         submission.setPaternalBackground(s); break;
            case "backgroundChangeable":       submission.setBackgroundChangeable(parseTriBool(s)); break;
            case "backgroundChangeConcerns":   submission.setBackgroundChangeConcerns(s); break;
            case "unreportedFeaturesDetails":  submission.setUnreportedFeaturesDetails(s); break;
            case "additionalInfo":             submission.setAdditionalInfo(s); break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown field: " + field);
        }
        HibernateUtil.currentSession().merge(submission);
        HibernateUtil.flushAndCommitCurrentSession();

        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("field", field);
        result.put("value", s);
        return result;
    }

    private static Boolean parseTriBool(String s) {
        if (s == null || s.isBlank()) return null;
        if ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)) return Boolean.TRUE;
        if ("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s)) return Boolean.FALSE;
        return null;
    }

    /**
     * JSON autocomplete for the "add submitter" modal on the line-submission detail page.
     * Returns a list of {label, value, fullName} entries suitable for jQuery UI autocomplete:
     * "label" is shown in the dropdown ("Pich, Christian (ZDB-PERS-060413-1)"), "value" is
     * the person's full name (placed back into the input), and "fullName"/"zdbID" are picked
     * up by the select-handler to POST the add request.
     */
    @RequestMapping(value = "/persons/search", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, String>> searchPersons(@RequestParam(value = "term", required = false) String term) {
        if (term == null || term.isBlank()) {
            return Collections.emptyList();
        }
        List<Person> people = HibernateUtil.currentSession()
                .createQuery(
                    "from Person where lower(fullName) like :q order by fullName",
                    Person.class)
                .setParameter("q", "%" + term.toLowerCase() + "%")
                .setMaxResults(20)
                .list();
        List<Map<String, String>> out = new java.util.ArrayList<>();
        for (Person p : people) {
            Map<String, String> entry = new HashMap<>();
            entry.put("label", p.getFullName() + " (" + p.getZdbID() + ")");
            entry.put("value", p.getFullName());
            entry.put("zdbID", p.getZdbID());
            entry.put("fullName", p.getFullName());
            out.add(entry);
        }
        return out;
    }

    /**
     * Add a person to a line submission as a submitter (POST). Idempotent — returns silently
     * if the (submission, person, role) tuple already exists.
     */
    @RequestMapping(value = "/line-submission/{zdbID}/add-submitter", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> addSubmitter(@PathVariable String zdbID,
                                            @RequestParam("personZdbID") String personZdbID) {
        LineSubmission submission = HibernateUtil.currentSession().get(LineSubmission.class, zdbID);
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Line submission " + zdbID + " not found");
        }
        Person person = HibernateUtil.currentSession().get(Person.class, personZdbID);
        if (person == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person " + personZdbID + " not found");
        }
        // Skip if already linked as submitter.
        boolean alreadyLinked = submission.getPersons().stream()
                .anyMatch(lsp -> lsp.getPerson().getZdbID().equals(personZdbID)
                        && "submitter".equals(lsp.getRole()));
        if (!alreadyLinked) {
            LineSubmissionPerson lsp = new LineSubmissionPerson();
            lsp.setLineSubmission(submission);
            lsp.setPerson(person);
            lsp.setRole("submitter");
            lsp.setSortOrder(submission.getPersons().size() + 1);
            HibernateUtil.createTransaction();
            HibernateUtil.currentSession().persist(lsp);
            HibernateUtil.flushAndCommitCurrentSession();
        }
        Map<String, String> result = new HashMap<>();
        result.put("status", "ok");
        result.put("personZdbID", personZdbID);
        result.put("personName", person.getFullName());
        return result;
    }

}
