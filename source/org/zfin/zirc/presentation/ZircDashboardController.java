package org.zfin.zirc.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.Updates;
import org.zfin.profile.Person;
import org.zfin.repository.RepositoryFactory;
import org.zfin.profile.service.ProfileService;
import org.zfin.zirc.entity.LineSubmission;
import org.zfin.zirc.entity.LineSubmissionPerson;
import org.zfin.zirc.entity.Mutation;
import org.zfin.zirc.api.ZircAssayFormSchema;
import org.zfin.zirc.api.ZircFormSchema;
import org.zfin.zirc.api.ZircGeneFormSchema;
import org.zfin.zirc.api.ZircLesionFormSchema;
import org.zfin.zirc.api.ZircMutationFormSchema;
import org.zfin.zirc.api.ZircPhenotypeFormSchema;
import org.zfin.zirc.service.GeneStatusComputer;
import org.zfin.zirc.service.GenotypingAssayStatusComputer;
import org.zfin.zirc.service.LesionStatusComputer;
import org.zfin.zirc.service.PhenotypeStatusComputer;
import org.zfin.zirc.service.LineSubmissionStatusComputer;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;
import org.zfin.zirc.service.MutationStatusComputer;
import org.zfin.zirc.service.SchemaSections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/zirc")
public class ZircDashboardController {

    @Autowired
    private org.zfin.zirc.service.ZircCommentService commentService;

    @GetMapping("/dashboard")
    public String viewDashboard(Model model) {
        List<LineSubmission> submissions = HibernateUtil.currentSession()
                .createQuery(
                    "from ZircLineSubmission where deletedAt is null order by createdAt desc",
                    LineSubmission.class)
                .list();
        // Per-row overall status, keyed by zdbID so the JSP can look it up cheaply.
        Map<String, FieldStatus> overallByZdbId = new LinkedHashMap<>();
        for (LineSubmission s : submissions) {
            overallByZdbId.put(s.getZdbID(), LineSubmissionStatusComputer.compute(s).overall());
        }
        // No "closed" column on LineSubmission yet, so everything lands in "Active".
        model.addAttribute("activeSubmissions", submissions);
        model.addAttribute("closedSubmissions", Collections.emptyList());
        model.addAttribute("overallStatus", overallByZdbId);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Line Submission Dashboard");
        return "zirc/dashboard";
    }

    /**
     * Schema-driven read-only detail view — the canonical line-submission
     * detail page. Reuses the existing {@link LineSubmissionStatusComputer}
     * maps and the central {@code Updates} table; serializes both into a
     * single JSON blob that the React mount reads off the DOM.
     */
    @GetMapping("/line-submission/{zdbID}")
    public String viewLineSubmissionReact(@PathVariable String zdbID, Model model) {
        LineSubmission submission = HibernateUtil.currentSession().get(LineSubmission.class, zdbID);
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Line submission " + zdbID + " not found");
        }
        model.addAttribute("submission", submission);

        // Status payload (field/section/overall + comment overlay) is built
        // once here and also served live via GET .../status so the React
        // page can refetch it when a comment's open/closed state changes.
        Map<String, Object> payload = buildStatusPayload(submission);

        // Left-nav subsections: one entry per mutation under "Mutations".
        // navigationItem.tag links each to '#' + makeDomIdentifier(title), so
        // "Mutation 1" -> #mutation-1, matching the id we set on the card div
        // in MutationsListRenderer's view-mode branch.
        //
        // Sub-sub-sections: each mutation's inner Groups (General, Mutagenesis,
        // …). dataPage.tag builds the href as '#'+makeDomIdentifier(parent)+
        // '-'+makeDomIdentifier(child), so "Mutation 1" / "General" links to
        // '#mutation-1-general' — matching the prefixed id SectionRenderer
        // emits when its config.idPrefix is set.
        List<String> mutationLabels = new ArrayList<>();
        Map<String, List<String>> mutationSubSubSections = new LinkedHashMap<>();
        Map<String, FieldStatus> mutationSubSectionStatus = new LinkedHashMap<>();
        Map<String, Map<String, FieldStatus>> mutationSubSubSectionStatus = new LinkedHashMap<>();
        List<String> mutationInnerGroups = List.of(
                "General", "Mutagenesis", "Lethality",
                "Genes", "Lesions", "Genotyping Assays", "Phenotypes",
                "Publications");
        if (submission.getMutations() != null) {
            int i = 1;
            for (Mutation mut : submission.getMutations()) {
                String label = "Mutation " + (i++);
                mutationLabels.add(label);
                mutationSubSubSections.put(label, mutationInnerGroups);
                // Per-mutation overall status; navigationItem.tag renders it
                // next to the sub-item via the zirc-status-badge tag.
                FieldStatusResult mutationStatus = MutationStatusComputer.compute(mut);
                mutationSubSectionStatus.put(label, mutationStatus.overall());
                // Per-Group status under each mutation. Keys must match the
                // inner-group labels above so dataPage's
                // subSubSectionStatus[sub][subsub] lookup hits.
                mutationSubSubSectionStatus.put(label, mutationStatus.bySection());
            }
        }
        if (!mutationLabels.isEmpty()) {
            model.addAttribute("subSections", Map.of("Mutations", mutationLabels));
            model.addAttribute("subSubSections", mutationSubSubSections);
            model.addAttribute("subSectionStatus", mutationSubSectionStatus);
            model.addAttribute("subSubSectionStatus", mutationSubSubSectionStatus);
        }

        try {
            String json = STATUS_PAYLOAD_MAPPER.writeValueAsString(payload);
            // Defang the </script close-tag pattern. JSON unescapes \/ to /, so
            // the parsed value on the React side is unchanged.
            json = json.replace("</", "<\\/");
            model.addAttribute("statusPayloadJson", json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize status payload", e);
        }

        String label = (submission.getName() != null && !submission.getName().isBlank())
                ? submission.getName() : zdbID;
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Line Submission: " + label);
        return "zirc/line-submission-detail-react";
    }

    /**
     * Live status payload for the React detail page. Same map embedded in
     * the JSP at first paint; the client refetches it when a comment's
     * open/closed state changes so the field/section/overall badges update
     * without a full reload.
     */
    @GetMapping("/line-submission/{zdbID}/status")
    @ResponseBody
    public Map<String, Object> lineSubmissionStatus(@PathVariable String zdbID) {
        LineSubmission submission = HibernateUtil.currentSession().get(LineSubmission.class, zdbID);
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Line submission " + zdbID + " not found");
        }
        return buildStatusPayload(submission);
    }

    /**
     * Compute the full status payload: submission + per-aggregate field /
     * section status, the per-mutation overall, all with the open-comment
     * IN_PROGRESS overlay folded into fields and propagated into section /
     * overall rollups.
     */
    private Map<String, Object> buildStatusPayload(LineSubmission submission) {
        FieldStatusResult status = LineSubmissionStatusComputer.compute(submission);

        Map<String, Map<String, Map<String, String>>> perMutationFieldStatus   = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, String>>> perMutationSectionStatus = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, String>>> perGeneFieldStatus       = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, String>>> perGeneSectionStatus     = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, String>>> perLesionFieldStatus     = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, String>>> perLesionSectionStatus   = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, String>>> perAssayFieldStatus      = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, String>>> perAssaySectionStatus    = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, String>>> perPhenotypeFieldStatus  = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, String>>> perPhenotypeSectionStatus = new LinkedHashMap<>();
        if (submission.getMutations() != null) {
            for (Mutation mut : submission.getMutations()) {
                FieldStatusResult r = MutationStatusComputer.compute(mut);
                String mutKey = String.valueOf(mut.getId());
                perMutationFieldStatus.put(mutKey,   fieldStatusToJson(r.byField()));
                perMutationSectionStatus.put(mutKey, fieldStatusToJson(r.bySection()));

                if (mut.getGenes() != null) {
                    for (org.zfin.zirc.entity.Gene g : mut.getGenes()) {
                        FieldStatusResult gr = GeneStatusComputer.compute(g);
                        String k = String.valueOf(g.getId());
                        perGeneFieldStatus.put(k,   fieldStatusToJson(gr.byField()));
                        perGeneSectionStatus.put(k, fieldStatusToJson(gr.bySection()));
                    }
                }
                if (mut.getLesions() != null) {
                    for (org.zfin.zirc.entity.Lesion lz : mut.getLesions()) {
                        FieldStatusResult lr = LesionStatusComputer.compute(lz);
                        String k = String.valueOf(lz.getId());
                        perLesionFieldStatus.put(k,   fieldStatusToJson(lr.byField()));
                        perLesionSectionStatus.put(k, fieldStatusToJson(lr.bySection()));
                    }
                }
                if (mut.getGenotypingAssays() != null) {
                    for (org.zfin.zirc.entity.GenotypingAssay ga : mut.getGenotypingAssays()) {
                        FieldStatusResult ar = GenotypingAssayStatusComputer.compute(ga);
                        String k = String.valueOf(ga.getId());
                        perAssayFieldStatus.put(k,   fieldStatusToJson(ar.byField()));
                        perAssaySectionStatus.put(k, fieldStatusToJson(ar.bySection()));
                    }
                }
                if (mut.getPhenotypes() != null) {
                    for (org.zfin.zirc.entity.Phenotype p : mut.getPhenotypes()) {
                        FieldStatusResult pr = PhenotypeStatusComputer.compute(p);
                        String k = String.valueOf(p.getId());
                        perPhenotypeFieldStatus.put(k,   fieldStatusToJson(pr.byField()));
                        perPhenotypeSectionStatus.put(k, fieldStatusToJson(pr.bySection()));
                    }
                }
            }
        }

        Map<String, Map<String, String>> submissionFieldStatus = fieldStatusToJson(status.byField());

        // Comment overlay: a field whose latest comment is still open
        // (closed=false) shows IN_PROGRESS regardless of its emptiness-derived
        // status. One bulk lookup across every recId in play.
        List<String> allRecIds = new ArrayList<>();
        allRecIds.add(submission.getZdbID());
        for (String id : perMutationFieldStatus.keySet())  allRecIds.add("ZIRC-MUT-" + id);
        for (String id : perGeneFieldStatus.keySet())       allRecIds.add("ZIRC-GENE-" + id);
        for (String id : perLesionFieldStatus.keySet())     allRecIds.add("ZIRC-LESION-" + id);
        for (String id : perAssayFieldStatus.keySet())      allRecIds.add("ZIRC-GA-" + id);
        for (String id : perPhenotypeFieldStatus.keySet())  allRecIds.add("ZIRC-PHEN-" + id);
        Set<String> openFieldKeys = commentService.openFieldKeys(allRecIds);

        overlayOpenComments(submissionFieldStatus, submission.getZdbID(), openFieldKeys);
        overlayOpenCommentsPerEntity(perMutationFieldStatus,  "ZIRC-MUT-",    openFieldKeys);
        overlayOpenCommentsPerEntity(perGeneFieldStatus,      "ZIRC-GENE-",   openFieldKeys);
        overlayOpenCommentsPerEntity(perLesionFieldStatus,    "ZIRC-LESION-", openFieldKeys);
        overlayOpenCommentsPerEntity(perAssayFieldStatus,     "ZIRC-GA-",     openFieldKeys);
        overlayOpenCommentsPerEntity(perPhenotypeFieldStatus, "ZIRC-PHEN-",   openFieldKeys);

        // Propagate the field overlay into section + overall rollups.
        Map<String, Map<String, String>> submissionSectionStatus = fieldStatusToJson(status.bySection());
        mergeFieldOverlayIntoSections(submissionSectionStatus, submissionFieldStatus,
                SchemaSections.groupsToFields(ZircFormSchema.uiSchema()));

        Map<String, List<String>> mutationSections = SchemaSections.groupsToFields(ZircMutationFormSchema.uiSchema());
        Map<String, List<String>> geneSections     = SchemaSections.groupsToFields(ZircGeneFormSchema.uiSchema());
        Map<String, List<String>> lesionSections   = SchemaSections.groupsToFields(ZircLesionFormSchema.uiSchema());
        Map<String, List<String>> assaySections    = SchemaSections.groupsToFields(ZircAssayFormSchema.uiSchema());
        Map<String, List<String>> phenoSections    = SchemaSections.groupsToFields(ZircPhenotypeFormSchema.uiSchema());

        // Bottom-up propagation so an open-comment IP on a leaf field climbs
        // all the way: leaf field → its section → that entity's overall →
        // the parent's child-rollup section → parent overall → … up to the
        // submission's "Mutations" section.
        //
        // 1. Fold the field overlay into each leaf aggregate's own sections,
        //    then take that entity's overall from its (now-overlaid) sections.
        Map<String, Map<String, String>> geneOverall      = new LinkedHashMap<>();
        Map<String, Map<String, String>> lesionOverall    = new LinkedHashMap<>();
        Map<String, Map<String, String>> assayOverall      = new LinkedHashMap<>();
        Map<String, Map<String, String>> phenotypeOverall = new LinkedHashMap<>();
        perGeneSectionStatus.forEach((id, sectionJson) -> {
            mergeFieldOverlayIntoSections(sectionJson, perGeneFieldStatus.get(id), geneSections);
            geneOverall.put(id, overallFromSections(sectionJson));
        });
        perLesionSectionStatus.forEach((id, sectionJson) -> {
            mergeFieldOverlayIntoSections(sectionJson, perLesionFieldStatus.get(id), lesionSections);
            lesionOverall.put(id, overallFromSections(sectionJson));
        });
        perAssaySectionStatus.forEach((id, sectionJson) -> {
            mergeFieldOverlayIntoSections(sectionJson, perAssayFieldStatus.get(id), assaySections);
            assayOverall.put(id, overallFromSections(sectionJson));
        });
        perPhenotypeSectionStatus.forEach((id, sectionJson) -> {
            mergeFieldOverlayIntoSections(sectionJson, perPhenotypeFieldStatus.get(id), phenoSections);
            phenotypeOverall.put(id, overallFromSections(sectionJson));
        });

        // 2. Per mutation: fold its own field overlay into its sections, then
        //    fold each child group's post-overlay overalls into the matching
        //    child-rollup section (worse-of keeps the computer's MISSING for
        //    a required-but-absent child collection), then take the mutation
        //    overall from its sections.
        Map<String, Map<String, String>> mutationOverallJson = new LinkedHashMap<>();
        if (submission.getMutations() != null) {
            for (Mutation mut : submission.getMutations()) {
                String mid = String.valueOf(mut.getId());
                Map<String, Map<String, String>> msec = perMutationSectionStatus.get(mid);
                mergeFieldOverlayIntoSections(msec, perMutationFieldStatus.get(mid), mutationSections);
                mergeChildOverall(msec, "Genes",             childOveralls(mut.getGenes(),            geneOverall));
                mergeChildOverall(msec, "Lesions",           childOveralls(mut.getLesions(),          lesionOverall));
                mergeChildOverall(msec, "Genotyping Assays", childOveralls(mut.getGenotypingAssays(), assayOverall));
                mergeChildOverall(msec, "Phenotypes",        childOveralls(mut.getPhenotypes(),       phenotypeOverall));
                Map<String, String> overall = overallFromSections(msec);
                if (overall != null) mutationOverallJson.put(mid, overall);
            }
            // 3. Submission "Mutations" section ← worse-of mutation overalls.
            Map<String, String> mutationsWorst = submissionSectionStatus.get("Mutations");
            for (Mutation mut : submission.getMutations()) {
                mutationsWorst = worseJson(mutationsWorst, mutationOverallJson.get(String.valueOf(mut.getId())));
            }
            if (mutationsWorst != null) submissionSectionStatus.put("Mutations", mutationsWorst);
        }

        // Field -> section name, used by the right-hand Change-History panel
        // to label each audit entry with its containing top-level section.
        // Inverted from SchemaSections.groupsToFields(ZircFormSchema.uiSchema()).
        Map<String, String> fieldSectionMap = new LinkedHashMap<>();
        SchemaSections.groupsToFields(ZircFormSchema.uiSchema())
                .forEach((section, fields) -> fields.forEach(f -> fieldSectionMap.put(f, section)));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("fieldStatus",     submissionFieldStatus);
        payload.put("sectionStatus",   submissionSectionStatus);
        payload.put("fieldSectionMap", fieldSectionMap);
        payload.put("mutationFieldStatus",   perMutationFieldStatus);
        payload.put("mutationSectionStatus", perMutationSectionStatus);
        payload.put("mutationOverallStatus", mutationOverallJson);
        payload.put("geneFieldStatus",       perGeneFieldStatus);
        payload.put("geneSectionStatus",     perGeneSectionStatus);
        payload.put("lesionFieldStatus",     perLesionFieldStatus);
        payload.put("lesionSectionStatus",   perLesionSectionStatus);
        payload.put("assayFieldStatus",      perAssayFieldStatus);
        payload.put("assaySectionStatus",    perAssaySectionStatus);
        payload.put("phenotypeFieldStatus",  perPhenotypeFieldStatus);
        payload.put("phenotypeSectionStatus", perPhenotypeSectionStatus);
        return payload;
    }

    private static final ObjectMapper STATUS_PAYLOAD_MAPPER = new ObjectMapper();

    /** JSON form of IN_PROGRESS, derived from the enum to avoid drift. */
    private static final Map<String, String> IN_PROGRESS_JSON = Map.of(
            "abbreviation", FieldStatus.IN_PROGRESS.getAbbreviation(),
            "displayName",  FieldStatus.IN_PROGRESS.getDisplayName(),
            "cssClass",     FieldStatus.IN_PROGRESS.getCssClass());

    /** Overlay IN_PROGRESS on a single recId's field-status JSON map for any
     *  field whose latest comment is open. */
    private static void overlayOpenComments(Map<String, Map<String, String>> fieldJson,
                                            String recId, Set<String> openFieldKeys) {
        for (String field : fieldJson.keySet()) {
            if (openFieldKeys.contains(org.zfin.zirc.service.ZircCommentService.fieldKey(recId, field))) {
                fieldJson.put(field, IN_PROGRESS_JSON);
            }
        }
    }

    /** Same as {@link #overlayOpenComments} for a per-entity map keyed by id,
     *  reconstructing each entity's recId from {@code recIdPrefix + id}. */
    private static void overlayOpenCommentsPerEntity(
            Map<String, Map<String, Map<String, String>>> perEntity,
            String recIdPrefix, Set<String> openFieldKeys) {
        perEntity.forEach((id, fieldJson) ->
                overlayOpenComments(fieldJson, recIdPrefix + id, openFieldKeys));
    }

    /** Severity rank for a status JSON (lower = worse), matching
     *  {@code FieldStatus.worse}: MISSING < IN_PROGRESS < COMPLETE < APPROVED. */
    private static int statusRank(Map<String, String> st) {
        if (st == null) return 9;
        return switch (st.getOrDefault("abbreviation", "")) {
            case "M"  -> 0;
            case "IP" -> 1;
            case "C"  -> 2;
            case "A"  -> 3;
            default   -> 9;
        };
    }

    private static Map<String, String> worseJson(Map<String, String> a, Map<String, String> b) {
        if (a == null) return b;
        if (b == null) return a;
        return statusRank(a) <= statusRank(b) ? a : b;
    }

    /**
     * Fold each section's field overlay into its status: for every
     * {@code section -> [field,…]} entry, replace the section's badge with
     * worst-of(existing badge, each present field's badge). Sections whose
     * fields aren't in {@code fieldJson} (e.g. list-widget groups like
     * "Genes") keep their existing child-rollup badge untouched.
     */
    private static void mergeFieldOverlayIntoSections(
            Map<String, Map<String, String>> sectionJson,
            Map<String, Map<String, String>> fieldJson,
            Map<String, List<String>> sectionToFields) {
        if (fieldJson == null) return;
        sectionToFields.forEach((section, fields) -> {
            Map<String, String> worst = sectionJson.get(section);
            for (String f : fields) {
                worst = worseJson(worst, fieldJson.get(f));
            }
            if (worst != null) sectionJson.put(section, worst);
        });
    }

    private static Map<String, String> overallFromSections(Map<String, Map<String, String>> sectionJson) {
        Map<String, String> worst = null;
        for (Map<String, String> s : sectionJson.values()) {
            worst = worseJson(worst, s);
        }
        return worst;
    }

    /** Fold a worse-of child-overall into a parent's child-rollup section
     *  (e.g. mutation "Lesions"), preserving the computer's existing value
     *  (which already encodes "required child collection is empty ⇒ MISSING"). */
    private static void mergeChildOverall(Map<String, Map<String, String>> sectionJson,
                                          String section, Map<String, String> childWorst) {
        if (childWorst == null) return;
        sectionJson.merge(section, childWorst, ZircDashboardController::worseJson);
    }

    /** Worse-of the post-overlay overalls of a child collection, looked up by
     *  id in {@code overallById}. Null when the collection is empty/absent. */
    private static <T> Map<String, String> childOveralls(
            java.util.Collection<T> children,
            Map<String, Map<String, String>> overallById) {
        if (children == null || children.isEmpty()) return null;
        Map<String, String> worst = null;
        for (T child : children) {
            Long id = entityId(child);
            if (id != null) worst = worseJson(worst, overallById.get(String.valueOf(id)));
        }
        return worst;
    }

    private static Long entityId(Object child) {
        if (child instanceof org.zfin.zirc.entity.Gene g)             return g.getId();
        if (child instanceof org.zfin.zirc.entity.Lesion l)           return l.getId();
        if (child instanceof org.zfin.zirc.entity.GenotypingAssay a)  return a.getId();
        if (child instanceof org.zfin.zirc.entity.Phenotype p)        return p.getId();
        return null;
    }

    private static Map<String, Map<String, String>> fieldStatusToJson(Map<String, FieldStatus> in) {
        Map<String, Map<String, String>> out = new LinkedHashMap<>();
        if (in == null) return out;
        in.forEach((k, v) -> {
            if (v == null) return;
            Map<String, String> m = new LinkedHashMap<>();
            m.put("abbreviation", v.getAbbreviation());
            m.put("displayName",  v.getDisplayName());
            m.put("cssClass",     v.getCssClass());
            out.put(k, m);
        });
        return out;
    }

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

    // ─────────────────────────────────────────────────────────────────────
    // Mutations: editor sub-page + per-field save + add/remove.
    // The parent line submission's edit page links here; mutations get their
    // own URL because the per-mutation field set is large enough that
    // expanding inline on the parent would be unwieldy.
    // ─────────────────────────────────────────────────────────────────────

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

    /**
     * Person autocomplete for the "add submitter" modal on the
     * line-submission detail page. Returns a list of
     * {label, value, fullName, zdbID} entries suitable for jQuery UI
     * autocomplete. The React app uses /api/zirc/autocomplete/persons
     * instead; this endpoint stays only because the legacy detail JSP
     * still mounts the jQuery UI modal.
     */
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
     * if the (submission, person) row already exists with that role.
     */
    @PostMapping("/line-submission/{zdbID}/add-submitter")
    @ResponseBody
    public Map<String, String> addSubmitter(@PathVariable String zdbID,
                                            @RequestParam("personZdbID") String personZdbID) {
        return addPersonWithRole(zdbID, personZdbID, org.zfin.zirc.entity.LineSubmissionRole.SUBMITTER);
    }

    /**
     * Add a person to a line submission as a PI (POST). Same shape as add-submitter;
     * separate endpoint per the ZFIN-10325 product directive to keep the PI picker
     * visually distinct from the submitter picker in the curation UI.
     */
    @PostMapping("/line-submission/{zdbID}/add-pi")
    @ResponseBody
    public Map<String, String> addPi(@PathVariable String zdbID,
                                     @RequestParam("personZdbID") String personZdbID) {
        return addPersonWithRole(zdbID, personZdbID, org.zfin.zirc.entity.LineSubmissionRole.PI);
    }

    private Map<String, String> addPersonWithRole(String zdbID, String personZdbID,
                                                  org.zfin.zirc.entity.LineSubmissionRole role) {
        LineSubmission submission = HibernateUtil.currentSession().get(LineSubmission.class, zdbID);
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Line submission " + zdbID + " not found");
        }
        Person person = HibernateUtil.currentSession().get(Person.class, personZdbID);
        if (person == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person " + personZdbID + " not found");
        }
        // Skip if already linked at all on this submission — the DB enforces
        // one role per person via the UNIQUE (submission, person) constraint
        // added by the ZFIN-10325 migration, so this client-side gate just
        // returns a friendlier 200 instead of letting the insert blow up.
        boolean alreadyLinked = submission.getPersons().stream()
                .anyMatch(lsp -> lsp.getPerson().getZdbID().equals(personZdbID));
        if (!alreadyLinked) {
            LineSubmissionPerson lsp = new LineSubmissionPerson();
            lsp.setLineSubmission(submission);
            lsp.setPerson(person);
            lsp.setRole(role.wire());
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
        result.put("role", role.wire());
        result.put("personZdbID", personZdbID);
        result.put("personName", person.getFullName());
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────
    // ──────────────────────────────────────────────────────────────────
    // Curator/submitter comment threads (per-field + per-section).
    //
    // Threads are addressed by (recId, scope, fieldName | sectionName).
    // recId reuses the existing audit identifier scheme so a comment can
    // attach to a top-level field (recId = ZDB-LINESUBMISSION-…), a
    // mutation (ZIRC-MUT-N), a gene/lesion/assay/phenotype, or a linked
    // feature.
    // ──────────────────────────────────────────────────────────────────

    /** JSON shape returned by the comment list endpoint. */
    public static record CommentDTO(Long id,
                                    String author,        // display name "F. Last"
                                    String authorZdbId,
                                    String comment,
                                    String createdAt) {}  // ISO-8601

    private static CommentDTO toDto(org.zfin.zirc.entity.LineSubmissionComment c) {
        String displayName;
        if (c.getAuthor() == null) {
            displayName = "Unknown";
        } else {
            String first = c.getAuthor().getFirstName();
            String last  = c.getAuthor().getLastName();
            String initial = (first != null && !first.isBlank()) ? first.substring(0, 1) + ". " : "";
            displayName = (initial + (last == null ? "" : last)).trim();
            if (displayName.isEmpty()) displayName = c.getAuthor().getFullName();
        }
        return new CommentDTO(
                c.getId(),
                displayName,
                c.getAuthor() == null ? null : c.getAuthor().getZdbID(),
                c.getComment(),
                c.getCreatedAt() == null ? null : c.getCreatedAt().toInstant().toString());
    }

    @GetMapping("/comments")
    @ResponseBody
    public List<CommentDTO> listComments(@RequestParam("recId") String recId,
                                         @RequestParam("scope") String scope,
                                         @RequestParam(value = "fieldName",   required = false) String fieldName,
                                         @RequestParam(value = "sectionName", required = false) String sectionName) {
        if (!"field".equals(scope) && !"section".equals(scope)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scope must be 'field' or 'section'");
        }
        String hql = "from ZircLineSubmissionComment where recId = :rid and scope = :scope and "
                + ("field".equals(scope) ? "fieldName = :name" : "sectionName = :name")
                + " order by createdAt asc, id asc";
        String name = "field".equals(scope) ? fieldName : sectionName;
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "fieldName (scope=field) or sectionName (scope=section) is required");
        }
        List<org.zfin.zirc.entity.LineSubmissionComment> rows =
                HibernateUtil.currentSession()
                        .createQuery(hql, org.zfin.zirc.entity.LineSubmissionComment.class)
                        .setParameter("rid", recId)
                        .setParameter("scope", scope)
                        .setParameter("name", name)
                        .list();
        List<CommentDTO> out = new ArrayList<>(rows.size());
        for (org.zfin.zirc.entity.LineSubmissionComment c : rows) out.add(toDto(c));
        return out;
    }

    @PostMapping("/comments")
    @ResponseBody
    public CommentDTO addComment(@RequestParam("recId") String recId,
                                 @RequestParam("scope") String scope,
                                 @RequestParam(value = "fieldName",   required = false) String fieldName,
                                 @RequestParam(value = "sectionName", required = false) String sectionName,
                                 @RequestParam("comment") String comment) {
        if (!"field".equals(scope) && !"section".equals(scope)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scope must be 'field' or 'section'");
        }
        String name = "field".equals(scope) ? fieldName : sectionName;
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "fieldName (scope=field) or sectionName (scope=section) is required");
        }
        if (comment == null || comment.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "comment must not be empty");
        }
        Person currentUser = ProfileService.getCurrentSecurityUser();
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required to comment");
        }
        HibernateUtil.createTransaction();
        try {
            org.zfin.zirc.entity.LineSubmissionComment c = new org.zfin.zirc.entity.LineSubmissionComment();
            c.setRecId(recId);
            c.setScope(scope);
            if ("field".equals(scope)) c.setFieldName(name);
            else                       c.setSectionName(name);
            c.setAuthor(HibernateUtil.currentSession().getReference(Person.class, currentUser.getZdbID()));
            c.setComment(comment.trim());
            HibernateUtil.currentSession().persist(c);
            HibernateUtil.flushAndCommitCurrentSession();
            return toDto(c);
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Per-section curator-approval flag.
    // Upserts a row in zirc.line_submission_section_approval and also
    // writes an audit-log entry so the existing history popups pick it
    // up. recId reuses the audit identifier scheme (so we can approve
    // submission-level sections AND per-mutation inner sections with
    // the same endpoint).
    // ──────────────────────────────────────────────────────────────────

    public static record SectionApprovalDTO(String recId,
                                            String sectionName,
                                            boolean approved,
                                            String approver,
                                            String approvedAt) {}

    // Allowlist of valid section slugs (mirrors what the JSP emits).
    private static final java.util.Set<String> TOP_LEVEL_SECTION_SLUGS = java.util.Set.of(
            "overview", "mutations", "linked-features", "background", "additional-info");
    private static final java.util.Set<String> MUTATION_SECTION_SLUGS = java.util.Set.of(
            "mutation", "overview", "genes", "lesions",
            "genotyping-assays", "phenotypes", "lethality", "publications");
    private static final java.util.Map<String, String> SECTION_SLUG_TO_LABEL = java.util.Map.ofEntries(
            java.util.Map.entry("overview",          "Overview"),
            java.util.Map.entry("mutations",         "Mutations"),
            java.util.Map.entry("linked-features",   "Linked Features"),
            java.util.Map.entry("background",        "Background"),
            java.util.Map.entry("additional-info",   "Additional Info"),
            java.util.Map.entry("genes",             "Genes"),
            java.util.Map.entry("lesions",           "Lesions"),
            java.util.Map.entry("genotyping-assays", "Genotyping Assays"),
            java.util.Map.entry("phenotypes",        "Phenotypes"),
            java.util.Map.entry("lethality",         "Lethality"),
            java.util.Map.entry("publications",      "Publications"));
    private static final java.util.regex.Pattern LSUB_REC_ID_PATTERN =
            java.util.regex.Pattern.compile("^ZDB-LINESUBMISSION-\\d{6}-\\d+$");
    private static final java.util.regex.Pattern MUT_REC_ID_PATTERN =
            java.util.regex.Pattern.compile("^ZIRC-MUT-(\\d+)$");

    @PostMapping("/section-approval")
    @ResponseBody
    public SectionApprovalDTO setSectionApproval(@RequestParam("recId") String recId,
                                                 @RequestParam("sectionName") String sectionName,
                                                 @RequestParam("approved") boolean approved) {
        if (recId == null || recId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "recId required");
        }
        if (sectionName == null || sectionName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sectionName required");
        }
        Person currentUser = ProfileService.getCurrentSecurityUser();
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        // Only curators may flip approvals.
        if (currentUser.getAccountInfo() == null || !currentUser.getAccountInfo().isCurator()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Curator role required");
        }
        // Validate (recId, sectionName) and resolve owning submission.
        LineSubmission owningSubmission;
        java.util.regex.Matcher mutMatch = MUT_REC_ID_PATTERN.matcher(recId);
        if (LSUB_REC_ID_PATTERN.matcher(recId).matches()) {
            if (!TOP_LEVEL_SECTION_SLUGS.contains(sectionName)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unknown top-level section: " + sectionName);
            }
            owningSubmission = HibernateUtil.currentSession().get(LineSubmission.class, recId);
            if (owningSubmission == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission " + recId + " not found");
            }
        } else if (mutMatch.matches()) {
            if (!MUTATION_SECTION_SLUGS.contains(sectionName)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unknown mutation section: " + sectionName);
            }
            Long mutId = Long.parseLong(mutMatch.group(1));
            Mutation mut = HibernateUtil.currentSession().get(Mutation.class, mutId);
            if (mut == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mutation " + mutId + " not found");
            }
            owningSubmission = mut.getLineSubmission();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "recId must be ZDB-LINESUBMISSION-* or ZIRC-MUT-*");
        }
        // Server-side rollup check: only allow approved=true when the
        // section's underlying status is already COMPLETE (or already
        // APPROVED — the latter lets an idempotent re-approval succeed
        // even if the overlay has already promoted the rollup). Un-approval
        // is always allowed.
        if (approved) {
            FieldStatus rollup = resolveSectionRollup(owningSubmission, recId, sectionName);
            if (rollup == null
                    || (rollup != FieldStatus.COMPLETE && rollup != FieldStatus.APPROVED)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Section " + sectionName + " is not Complete; cannot approve");
            }
        }
        HibernateUtil.createTransaction();
        try {
            org.zfin.zirc.entity.LineSubmissionSectionApproval row = HibernateUtil.currentSession()
                    .createQuery(
                        "from ZircLineSubmissionSectionApproval where recId = :rid and sectionName = :sn",
                        org.zfin.zirc.entity.LineSubmissionSectionApproval.class)
                    .setParameter("rid", recId)
                    .setParameter("sn", sectionName)
                    .uniqueResult();
            boolean oldApproved = row != null && row.isApproved();
            if (row == null) {
                row = new org.zfin.zirc.entity.LineSubmissionSectionApproval();
                row.setRecId(recId);
                row.setSectionName(sectionName);
            }
            row.setApproved(approved);
            row.setApprover(HibernateUtil.currentSession().getReference(Person.class, currentUser.getZdbID()));
            row.setApprovedAt(new java.util.Date());
            row = (org.zfin.zirc.entity.LineSubmissionSectionApproval)
                    HibernateUtil.currentSession().merge(row);
            if (oldApproved != approved) {
                RepositoryFactory.getInfrastructureRepository().insertUpdatesTable(
                        recId, "section.approved." + sectionName,
                        Boolean.toString(oldApproved), Boolean.toString(approved),
                        "Section " + sectionName + (approved ? " approved" : " un-approved"));
            }
            HibernateUtil.flushAndCommitCurrentSession();
            String approverName = currentUser.getFullName();
            return new SectionApprovalDTO(recId, sectionName, approved,
                    approverName,
                    row.getApprovedAt() == null ? null : row.getApprovedAt().toInstant().toString());
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    /** Resolve the current rollup status for (recId, sectionName) by running the
     *  relevant status computer on the owning submission. Returns null if the
     *  section can't be located. Pre-approval-overlay value — the caller decides
     *  whether to allow the toggle based on it. */
    private FieldStatus resolveSectionRollup(LineSubmission submission, String recId, String sectionName) {
        String label = SECTION_SLUG_TO_LABEL.get(sectionName);
        if (LSUB_REC_ID_PATTERN.matcher(recId).matches()) {
            if (label == null) return null;
            FieldStatusResult r = LineSubmissionStatusComputer.compute(submission);
            return r.bySection().get(label);
        }
        java.util.regex.Matcher m = MUT_REC_ID_PATTERN.matcher(recId);
        if (m.matches()) {
            long mid = Long.parseLong(m.group(1));
            Mutation mut = submission.getMutations() == null ? null :
                    submission.getMutations().stream()
                            .filter(x -> x.getId() == mid).findFirst().orElse(null);
            if (mut == null) return null;
            FieldStatusResult r = MutationStatusComputer.compute(mut);
            if ("mutation".equals(sectionName)) {
                return r.overall();
            }
            return label == null ? null : r.bySection().get(label);
        }
        return null;
    }
}
