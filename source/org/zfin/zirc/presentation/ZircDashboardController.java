package org.zfin.zirc.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.zirc.entity.LineSubmission;
import org.zfin.zirc.entity.LineSubmissionPerson;
import org.zfin.zirc.entity.Mutation;
import org.zfin.zirc.service.LineSubmissionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/zirc")
public class ZircDashboardController {

    @Autowired
    private LineSubmissionService lineSubmissionService;

    @GetMapping("/dashboard")
    public String viewDashboard(Model model) {
        List<LineSubmission> submissions = HibernateUtil.currentSession()
                .createQuery(
                    "from ZircLineSubmission where deletedAt is null order by createdAt desc",
                    LineSubmission.class)
                .list();
        // No status column on LineSubmission yet, so everything lands in "Active".
        model.addAttribute("activeSubmissions", submissions);
        model.addAttribute("closedSubmissions", Collections.emptyList());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Line Submission Dashboard");
        return "zirc/dashboard";
    }

    @GetMapping("/line-submission/{zdbID}")
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
     * Render the edit page for a brand-new submission. No row is persisted here —
     * the React component starts with empty fields and {@link #saveField} creates
     * the row on the first save. The {@code submission} model attribute is a
     * transient (un-persisted) entity so the JSP can use the same EL access
     * patterns as for existing submissions.
     */
    @GetMapping("/line-submission/new")
    public String newLineSubmission(Model model) {
        model.addAttribute("submission", new LineSubmission());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "New Line Submission");
        return "zirc/line-submission-edit";
    }

    @GetMapping("/line-submission/{zdbID}/edit")
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
     * JSON snapshot of a line submission's editable scalar fields, consumed by the
     * React-driven edit page.
     */
    @GetMapping("/line-submission/{zdbID}.json")
    @ResponseBody
    public LineSubmissionDTO getLineSubmissionJson(@PathVariable String zdbID) {
        LineSubmission submission = HibernateUtil.currentSession().get(LineSubmission.class, zdbID);
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Line submission " + zdbID + " not found");
        }
        return LineSubmissionDTO.from(submission);
    }

    /**
     * Per-field save endpoint. If {@code zdbID} is omitted (or blank), a new
     * submission is created on the fly and returned with its freshly minted ZDB
     * ID — this is how the React form's "new submission" flow lands its first
     * save without a prior stub row. Returns the full DTO so the client can
     * pick up the new ID and any server-derived state.
     */
    @PostMapping("/line-submission/save-field")
    @ResponseBody
    public LineSubmissionDTO saveField(@RequestParam(value = "zdbID", required = false) String zdbID,
                                       @RequestParam("field") String field,
                                       @RequestParam(value = "value", required = false) String value) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        try {
            LineSubmission submission = lineSubmissionService.saveField(zdbID, field, value, currentUser);
            HibernateUtil.flushAndCommitCurrentSession();
            return LineSubmissionDTO.from(submission);
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Replace-all endpoint for the Acceptance Reasons section. Takes the full
     * desired list of canonical reason values (form-encoded multivalued
     * {@code reasons} param) plus the single free-text {@code reasonsOther}
     * entry. Like {@link #saveField}, an empty {@code zdbID} triggers
     * create-on-first-save.
     */
    @PostMapping("/line-submission/save-reasons")
    @ResponseBody
    public LineSubmissionDTO saveReasons(@RequestParam(value = "zdbID", required = false) String zdbID,
                                         @RequestParam(value = "reasons", required = false) String[] reasons,
                                         @RequestParam(value = "reasonsOther", required = false) String reasonsOther) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        try {
            LineSubmission submission = lineSubmissionService.saveReasons(zdbID, reasons, reasonsOther, currentUser);
            HibernateUtil.flushAndCommitCurrentSession();
            return LineSubmissionDTO.from(submission);
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Replace-all endpoint for the Linked Features section. Body is a JSON
     * array of {@link LinkedFeatureDTO} rows; {@code zdbID} comes via query
     * string so the React client uses the same create-on-first-save flow as
     * the other save endpoints. Empty / blank feature names are dropped
     * server-side.
     */
    @PostMapping(value = "/line-submission/save-linked-features",
                 consumes = "application/json")
    @ResponseBody
    public LineSubmissionDTO saveLinkedFeatures(@RequestParam(value = "zdbID", required = false) String zdbID,
                                                @RequestBody List<LinkedFeatureDTO> linkedFeatures) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        try {
            LineSubmission submission = lineSubmissionService.saveLinkedFeatures(zdbID, linkedFeatures, currentUser);
            HibernateUtil.flushAndCommitCurrentSession();
            return LineSubmissionDTO.from(submission);
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Mutations: editor sub-page + per-field save + add/remove.
    // The parent line submission's edit page links here; mutations get their
    // own URL because the per-mutation field set is large enough that
    // expanding inline on the parent would be unwieldy.
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Create-and-redirect: persists a fresh empty mutation under {@code lsId}
     * (creating the parent submission too if it doesn't exist yet, just like
     * {@link #saveField}), then sends the user to the mutation editor.
     */
    @GetMapping("/line-submission/{lsId}/mutation/new")
    public String newMutation(@PathVariable String lsId) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        try {
            Mutation m = lineSubmissionService.addMutation(lsId, currentUser);
            HibernateUtil.flushAndCommitCurrentSession();
            return "redirect:/action/zirc/mutation/" + m.getId() + "/edit";
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    @GetMapping("/mutation/{mutationId}/edit")
    public String editMutation(@PathVariable Long mutationId, Model model) {
        Mutation mutation = HibernateUtil.currentSession().get(Mutation.class, mutationId);
        if (mutation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mutation " + mutationId + " not found");
        }
        model.addAttribute("mutation", mutation);
        model.addAttribute("submission", mutation.getLineSubmission());
        String label = (mutation.getAlleleDesignation() != null && !mutation.getAlleleDesignation().isBlank())
                ? mutation.getAlleleDesignation()
                : "Mutation #" + mutation.getSortOrder();
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Edit Mutation: " + label);
        return "zirc/mutation-edit";
    }

    @GetMapping("/mutation/{mutationId}.json")
    @ResponseBody
    public MutationDTO getMutationJson(@PathVariable Long mutationId) {
        Mutation mutation = HibernateUtil.currentSession().get(Mutation.class, mutationId);
        if (mutation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mutation " + mutationId + " not found");
        }
        return MutationDTO.from(mutation);
    }

    @PostMapping("/mutation/save-field")
    @ResponseBody
    public MutationDTO saveMutationField(@RequestParam("mutationId") Long mutationId,
                                         @RequestParam("field") String field,
                                         @RequestParam(value = "value", required = false) String value) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        try {
            Mutation m = lineSubmissionService.saveMutationField(mutationId, field, value, currentUser);
            HibernateUtil.flushAndCommitCurrentSession();
            return MutationDTO.from(m);
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Replace-all save for a mutation's per-row gene list. Body is a JSON
     * array of {@link GeneDTO} rows; existing rows are diffed by persistent
     * id (preserving FK identity for unchanged rows). Returns the full
     * mutation DTO so the React form can refresh its gene list (and pick
     * up server-resolved Marker abbreviations) without a separate GET.
     */
    @PostMapping(value = "/mutation/{mutationId}/save-genes",
                 consumes = "application/json")
    @ResponseBody
    public MutationDTO saveGenes(@PathVariable Long mutationId,
                                 @RequestBody List<GeneDTO> genes) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        try {
            Mutation m = lineSubmissionService.saveGenes(mutationId, genes, currentUser);
            HibernateUtil.flushAndCommitCurrentSession();
            return MutationDTO.from(m);
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    @PostMapping(value = "/mutation/{mutationId}/save-lesions",
                 consumes = "application/json")
    @ResponseBody
    public MutationDTO saveLesions(@PathVariable Long mutationId,
                                   @RequestBody List<LesionDTO> lesions) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        try {
            Mutation m = lineSubmissionService.saveLesions(mutationId, lesions, currentUser);
            HibernateUtil.flushAndCommitCurrentSession();
            return MutationDTO.from(m);
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    @PostMapping(value = "/mutation/{mutationId}/save-genotyping-assays",
                 consumes = "application/json")
    @ResponseBody
    public MutationDTO saveGenotypingAssays(@PathVariable Long mutationId,
                                            @RequestBody List<GenotypingAssayDTO> assays) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        try {
            Mutation m = lineSubmissionService.saveGenotypingAssays(mutationId, assays, currentUser);
            HibernateUtil.flushAndCommitCurrentSession();
            return MutationDTO.from(m);
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    @PostMapping(value = "/mutation/{mutationId}/save-phenotypes",
                 consumes = "application/json")
    @ResponseBody
    public MutationDTO savePhenotypes(@PathVariable Long mutationId,
                                      @RequestBody List<PhenotypeDTO> phenotypes) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        try {
            Mutation m = lineSubmissionService.savePhenotypes(mutationId, phenotypes, currentUser);
            HibernateUtil.flushAndCommitCurrentSession();
            return MutationDTO.from(m);
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    @PostMapping(value = "/mutation/{mutationId}/save-publications",
                 consumes = "application/json")
    @ResponseBody
    public MutationDTO savePublications(@PathVariable Long mutationId,
                                        @RequestBody List<String> publications) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        try {
            Mutation m = lineSubmissionService.savePublications(mutationId, publications, currentUser);
            HibernateUtil.flushAndCommitCurrentSession();
            return MutationDTO.from(m);
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Delete a mutation. Returns the updated parent submission DTO so the
     * React form on the parent edit page can refresh its mutations list
     * without a separate GET.
     */
    @PostMapping("/mutation/{mutationId}/delete")
    @ResponseBody
    public LineSubmissionDTO deleteMutation(@PathVariable Long mutationId) {
        HibernateUtil.createTransaction();
        try {
            Mutation m = HibernateUtil.currentSession().get(Mutation.class, mutationId);
            if (m == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mutation " + mutationId + " not found");
            }
            LineSubmission parent = m.getLineSubmission();
            lineSubmissionService.removeMutation(mutationId);
            HibernateUtil.flushAndCommitCurrentSession();
            return LineSubmissionDTO.from(parent);
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    /**
     * JSON autocomplete for the "add submitter" modal on the line-submission detail page.
     * Returns a list of {label, value, fullName} entries suitable for jQuery UI autocomplete:
     * "label" is shown in the dropdown ("Pich, Christian (ZDB-PERS-060413-1)"), "value" is
     * the person's full name (placed back into the input), and "fullName"/"zdbID" are picked
     * up by the select-handler to POST the add request.
     */
    /**
     * Marker autocomplete for the mutation editor's "allele designation"
     * field (when "exists in ZFIN" is checked) and the gene rows' mutated
     * gene picker. Same shape as {@link #searchPersons}: returns a small
     * list of {label, value, zdbID} entries suitable for any typeahead
     * client.
     *
     * <p>Matches by abbreviation prefix or substring. Caps at 20 results.
     */
    @GetMapping("/markers/search")
    @ResponseBody
    public List<Map<String, String>> searchMarkers(@RequestParam(value = "term", required = false) String term) {
        if (term == null || term.isBlank()) {
            return Collections.emptyList();
        }
        List<org.zfin.marker.Marker> markers = HibernateUtil.currentSession()
                .createQuery(
                    "from Marker where lower(abbreviation) like :q order by abbreviation",
                    org.zfin.marker.Marker.class)
                .setParameter("q", "%" + term.toLowerCase() + "%")
                .setMaxResults(20)
                .list();
        List<Map<String, String>> out = new ArrayList<>();
        for (org.zfin.marker.Marker m : markers) {
            Map<String, String> entry = new HashMap<>();
            entry.put("label", m.getAbbreviation() + " (" + m.getZdbID() + ")");
            entry.put("value", m.getAbbreviation());
            entry.put("zdbID", m.getZdbID());
            out.add(entry);
        }
        return out;
    }

    /**
     * Chromosome-name autocomplete for the per-gene "linkage group" field.
     * Returns names from {@code public.chromosome} ordered by chrom_order.
     * The Linkage Group column is varchar(10) and accepts free-text values
     * like "NT" too — the autocomplete is a hint, not a constraint.
     */
    @GetMapping("/chromosomes/search")
    @ResponseBody
    public List<Map<String, String>> searchChromosomes(@RequestParam(value = "term", required = false) String term) {
        String t = term == null ? "" : term.trim().toLowerCase();
        String sql = t.isEmpty()
                ? "select chrom_name from chromosome order by chrom_order"
                : "select chrom_name from chromosome where lower(chrom_name) like :q order by chrom_order";
        var query = HibernateUtil.currentSession().createNativeQuery(sql, String.class);
        if (!t.isEmpty()) {
            query.setParameter("q", t + "%");
        }
        List<String> names = query.setMaxResults(30).list();
        List<Map<String, String>> out = new ArrayList<>();
        for (String name : names) {
            Map<String, String> entry = new HashMap<>();
            entry.put("label", name);
            entry.put("value", name);
            out.add(entry);
        }
        return out;
    }

    @GetMapping("/persons/search")
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
        List<Map<String, String>> out = new ArrayList<>();
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
    @PostMapping("/line-submission/{zdbID}/add-submitter")
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
            try {
                HibernateUtil.currentSession().persist(lsp);
                HibernateUtil.flushAndCommitCurrentSession();
            } catch (RuntimeException e) {
                HibernateUtil.rollbackTransaction();
                throw e;
            }
        }
        Map<String, String> result = new HashMap<>();
        result.put("status", "ok");
        result.put("personZdbID", personZdbID);
        result.put("personName", person.getFullName());
        return result;
    }

}
