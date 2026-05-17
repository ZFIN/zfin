package org.zfin.zirc.presentation;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.zfin.profile.service.ProfileService;
import org.zfin.zirc.entity.LineSubmission;
import org.zfin.zirc.entity.LineSubmissionPerson;
import org.zfin.zirc.entity.Mutation;
import org.zfin.zirc.service.GeneStatusComputer;
import org.zfin.zirc.service.GenotypingAssayStatusComputer;
import org.zfin.zirc.service.LesionStatusComputer;
import org.zfin.zirc.service.PhenotypeStatusComputer;
import org.zfin.zirc.service.LineSubmissionService;
import org.zfin.zirc.service.LineSubmissionStatusComputer;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;
import org.zfin.zirc.service.MutationStatusComputer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    @GetMapping("/line-submission/{zdbID}")
    public String viewLineSubmission(@PathVariable String zdbID, Model model) {
        LineSubmission submission = HibernateUtil.currentSession().get(LineSubmission.class, zdbID);
        if (submission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Line submission " + zdbID + " not found");
        }
        model.addAttribute("submission", submission);
        // FieldStatus enum carries its own abbreviation / cssClass / displayName;
        // the JSP tag reads them via EL getters. No flattening needed.
        FieldStatusResult status = LineSubmissionStatusComputer.compute(submission);
        model.addAttribute("fieldStatus", status.byField());
        model.addAttribute("sectionStatus", status.bySection());
        model.addAttribute("overallStatus", status.overall());

        // Per-mutation status, in one pass:
        //   mutationStatus        : Mutation.id  → overall status (subsection header badge)
        //   mutationFieldStatus   : Mutation.id  → byField map  (per-row badges)
        //   mutationSectionStatus : Mutation.id  → bySection map (per-subsection header badges)
        //   subSectionStatus      : "Mutation N" → overall status (left-nav sub-items)
        //   subSections           : "Mutations" → ["Mutation 1", "Mutation 2", …]
        Map<Long, FieldStatus> mutationStatus = new LinkedHashMap<>();
        Map<Long, Map<String, FieldStatus>> mutationFieldStatus = new LinkedHashMap<>();
        Map<Long, Map<String, FieldStatus>> mutationSectionStatus = new LinkedHashMap<>();
        Map<Long, Map<String, FieldStatus>> geneFieldStatus = new LinkedHashMap<>();
        Map<Long, Map<String, FieldStatus>> lesionFieldStatus = new LinkedHashMap<>();
        Map<Long, Map<String, FieldStatus>> assayFieldStatus = new LinkedHashMap<>();
        Map<Long, Map<String, FieldStatus>> phenotypeFieldStatus = new LinkedHashMap<>();
        Map<String, FieldStatus> subSectionStatus = new LinkedHashMap<>();
        // Third-level nav: each "Mutation N" expands into its 7 inner sections.
        // The seven labels match the <z:section title="…"> blocks under each
        // Mutation in line-submission-detail.jsp; anchor IDs follow the
        // convention {sub-id}-{sub-sub-id} which dataPage.tag builds from
        // makeDomIdentifier and the JSP applies via sectionID="…".
        Map<String, List<String>> subSubSections = new LinkedHashMap<>();
        Map<String, Map<String, FieldStatus>> subSubSectionStatus = new LinkedHashMap<>();
        List<String> mutationSubSectionLabels = List.of(
                "Overview", "Genes", "Lesions", "Genotyping Assays",
                "Phenotypes", "Lethality", "Publications");
        List<String> mutationLabels = new ArrayList<>();
        if (submission.getMutations() != null) {
            int i = 1;
            for (Mutation mut : submission.getMutations()) {
                String label = "Mutation " + i++;
                FieldStatusResult r = MutationStatusComputer.compute(mut);
                mutationStatus.put(mut.getId(), r.overall());
                mutationFieldStatus.put(mut.getId(), r.byField());
                mutationSectionStatus.put(mut.getId(), r.bySection());
                subSectionStatus.put(label, r.overall());
                mutationLabels.add(label);
                subSubSections.put(label, mutationSubSectionLabels);
                subSubSectionStatus.put(label, r.bySection());
                if (mut.getGenes() != null) {
                    for (org.zfin.zirc.entity.Gene g : mut.getGenes()) {
                        geneFieldStatus.put(g.getId(), GeneStatusComputer.compute(g).byField());
                    }
                }
                if (mut.getLesions() != null) {
                    for (org.zfin.zirc.entity.Lesion lz : mut.getLesions()) {
                        lesionFieldStatus.put(lz.getId(), LesionStatusComputer.compute(lz).byField());
                    }
                }
                if (mut.getGenotypingAssays() != null) {
                    for (org.zfin.zirc.entity.GenotypingAssay ga : mut.getGenotypingAssays()) {
                        assayFieldStatus.put(ga.getId(), GenotypingAssayStatusComputer.compute(ga).byField());
                    }
                }
                if (mut.getPhenotypes() != null) {
                    for (org.zfin.zirc.entity.Phenotype p : mut.getPhenotypes()) {
                        phenotypeFieldStatus.put(p.getId(), PhenotypeStatusComputer.compute(p).byField());
                    }
                }
            }
        }
        // History of field changes for this submission (for the in-row popups).
        // One query loads updates for the submission, its mutations, and all
        // sub-entities (genes, lesions, assays, phenotypes, linked features);
        // results are partitioned by recID prefix into per-entity maps so the
        // JSP can look up history for any field row.
        List<String> auditKeys = new ArrayList<>();
        auditKeys.add(submission.getZdbID());
        if (submission.getMutations() != null) {
            for (Mutation mut : submission.getMutations()) {
                auditKeys.add("ZIRC-MUT-" + mut.getId());
                if (mut.getGenes() != null) {
                    for (org.zfin.zirc.entity.Gene g : mut.getGenes()) {
                        auditKeys.add("ZIRC-GENE-" + g.getId());
                    }
                }
                if (mut.getLesions() != null) {
                    for (org.zfin.zirc.entity.Lesion lz : mut.getLesions()) {
                        auditKeys.add("ZIRC-LESION-" + lz.getId());
                    }
                }
                if (mut.getGenotypingAssays() != null) {
                    for (org.zfin.zirc.entity.GenotypingAssay ga : mut.getGenotypingAssays()) {
                        auditKeys.add("ZIRC-GA-" + ga.getId());
                    }
                }
                if (mut.getPhenotypes() != null) {
                    for (org.zfin.zirc.entity.Phenotype p : mut.getPhenotypes()) {
                        auditKeys.add("ZIRC-PHEN-" + p.getId());
                    }
                }
            }
        }
        if (submission.getLinkedFeatures() != null) {
            for (org.zfin.zirc.entity.LinkedFeature lf : submission.getLinkedFeatures()) {
                if (lf.getMutationA() != null && lf.getMutationB() != null) {
                    auditKeys.add("ZIRC-LF-" + lf.getMutationA().getId() + "-" + lf.getMutationB().getId());
                }
            }
        }
        List<Updates> allUpdates = HibernateUtil.currentSession()
                .createQuery(
                    "from Updates where recID in :rids order by whenUpdated desc",
                    Updates.class)
                .setParameterList("rids", auditKeys)
                .list();
        Map<String, List<Updates>> fieldUpdates = new LinkedHashMap<>();
        Map<Long, Map<String, List<Updates>>> mutationFieldUpdates    = new LinkedHashMap<>();
        Map<Long, Map<String, List<Updates>>> geneFieldUpdates        = new LinkedHashMap<>();
        Map<Long, Map<String, List<Updates>>> lesionFieldUpdates      = new LinkedHashMap<>();
        Map<Long, Map<String, List<Updates>>> assayFieldUpdates       = new LinkedHashMap<>();
        Map<Long, Map<String, List<Updates>>> phenotypeFieldUpdates   = new LinkedHashMap<>();
        Map<String, Map<String, List<Updates>>> linkedFeatureFieldUpdates = new LinkedHashMap<>();
        for (Updates u : allUpdates) {
            String recId = u.getRecID();
            if (recId == null) continue;
            if (submission.getZdbID().equals(recId)) {
                fieldUpdates.computeIfAbsent(u.getFieldName(), k -> new ArrayList<>()).add(u);
            } else if (recId.startsWith("ZIRC-MUT-")) {
                Long id = Long.parseLong(recId.substring("ZIRC-MUT-".length()));
                mutationFieldUpdates.computeIfAbsent(id, k -> new LinkedHashMap<>())
                        .computeIfAbsent(u.getFieldName(), k -> new ArrayList<>()).add(u);
            } else if (recId.startsWith("ZIRC-GENE-")) {
                Long id = Long.parseLong(recId.substring("ZIRC-GENE-".length()));
                geneFieldUpdates.computeIfAbsent(id, k -> new LinkedHashMap<>())
                        .computeIfAbsent(u.getFieldName(), k -> new ArrayList<>()).add(u);
            } else if (recId.startsWith("ZIRC-LESION-")) {
                Long id = Long.parseLong(recId.substring("ZIRC-LESION-".length()));
                lesionFieldUpdates.computeIfAbsent(id, k -> new LinkedHashMap<>())
                        .computeIfAbsent(u.getFieldName(), k -> new ArrayList<>()).add(u);
            } else if (recId.startsWith("ZIRC-GA-")) {
                Long id = Long.parseLong(recId.substring("ZIRC-GA-".length()));
                assayFieldUpdates.computeIfAbsent(id, k -> new LinkedHashMap<>())
                        .computeIfAbsent(u.getFieldName(), k -> new ArrayList<>()).add(u);
            } else if (recId.startsWith("ZIRC-PHEN-")) {
                Long id = Long.parseLong(recId.substring("ZIRC-PHEN-".length()));
                phenotypeFieldUpdates.computeIfAbsent(id, k -> new LinkedHashMap<>())
                        .computeIfAbsent(u.getFieldName(), k -> new ArrayList<>()).add(u);
            } else if (recId.startsWith("ZIRC-LF-")) {
                String key = recId.substring("ZIRC-LF-".length());
                linkedFeatureFieldUpdates.computeIfAbsent(key, k -> new LinkedHashMap<>())
                        .computeIfAbsent(u.getFieldName(), k -> new ArrayList<>()).add(u);
            }
        }
        model.addAttribute("fieldUpdates", fieldUpdates);
        model.addAttribute("mutationFieldUpdates", mutationFieldUpdates);
        model.addAttribute("geneFieldUpdates", geneFieldUpdates);
        model.addAttribute("lesionFieldUpdates", lesionFieldUpdates);
        model.addAttribute("assayFieldUpdates", assayFieldUpdates);
        model.addAttribute("phenotypeFieldUpdates", phenotypeFieldUpdates);
        model.addAttribute("linkedFeatureFieldUpdates", linkedFeatureFieldUpdates);

        // Section roll-ups: aggregate all field-level updates under each
        // visible section so the curator can open one modal per section
        // and see every audited change inside it (most-recent first).
        Map<String, List<Updates>> sectionUpdates = new LinkedHashMap<>();
        java.util.function.BiConsumer<String, Collection<List<Updates>>> rollup = (section, lists) -> {
            List<Updates> agg = sectionUpdates.computeIfAbsent(section, k -> new ArrayList<>());
            for (List<Updates> lst : lists) {
                if (lst != null) agg.addAll(lst);
            }
        };
        java.util.function.Function<String[], List<List<Updates>>> pickFields = keys -> {
            List<List<Updates>> out = new ArrayList<>();
            for (String k : keys) out.add(fieldUpdates.get(k));
            return out;
        };
        rollup.accept("Overview",
                pickFields.apply(new String[]{"name", "previousNames", "reasons", "reasonsOther", "abbreviation"}));
        rollup.accept("Background",
                pickFields.apply(new String[]{"maternalBackground", "paternalBackground",
                        "backgroundChangeable", "backgroundChangeConcerns"}));
        rollup.accept("Additional Info",
                pickFields.apply(new String[]{"additionalInfo", "unreportedFeaturesDetails",
                        "husbandryInfo", "singleAllelic"}));
        // Linked Features: every update on any LF row.
        for (Map<String, List<Updates>> byField : linkedFeatureFieldUpdates.values()) {
            rollup.accept("Linked Features", byField.values());
        }
        // Mutations: every mutation + every gene/lesion/assay/phenotype update across all mutations.
        for (Map<String, List<Updates>> byField : mutationFieldUpdates.values()) {
            rollup.accept("Mutations", byField.values());
        }
        for (Map<String, List<Updates>> byField : geneFieldUpdates.values()) {
            rollup.accept("Mutations", byField.values());
        }
        for (Map<String, List<Updates>> byField : lesionFieldUpdates.values()) {
            rollup.accept("Mutations", byField.values());
        }
        for (Map<String, List<Updates>> byField : assayFieldUpdates.values()) {
            rollup.accept("Mutations", byField.values());
        }
        for (Map<String, List<Updates>> byField : phenotypeFieldUpdates.values()) {
            rollup.accept("Mutations", byField.values());
        }
        for (List<Updates> lst : sectionUpdates.values()) {
            lst.sort((a, b) -> b.getWhenUpdated().compareTo(a.getWhenUpdated()));
        }
        model.addAttribute("sectionUpdates", sectionUpdates);

        // Per-mutation section roll-ups. For each mutation, build a map
        // from its inner section label (Overview, Genes, Lesions, ...) to
        // the consolidated list of updates inside it.
        Map<Long, Map<String, List<Updates>>> mutationSectionUpdates = new LinkedHashMap<>();
        String[] mutOverviewFields = {"alleleDesignation", "alleleInZfin", "mutagenesisStage",
                "mutagenesisProtocol", "mutagenesisProtocolOther", "molecularlyCharacterized",
                "mutationType", "zfinRecordEstablished", "cellGenomicFeature",
                "mutationDiscoverer", "mutationInstitution"};
        String[] mutLethalityFields = {"homozygousLethal", "lethalityStageTypical",
                "lethalitySpecificTimepoint", "lethalityWindowStart", "lethalityWindowEnd",
                "lethalityAdditionalInfo"};
        if (submission.getMutations() != null) {
            for (Mutation mut : submission.getMutations()) {
                Long mid = mut.getId();
                Map<String, List<Updates>> perField = mutationFieldUpdates.getOrDefault(mid, Map.of());
                Map<String, List<Updates>> mySections = new LinkedHashMap<>();
                List<Updates> overviewList = new ArrayList<>();
                for (String f : mutOverviewFields) {
                    List<Updates> u = perField.get(f);
                    if (u != null) overviewList.addAll(u);
                }
                if (!overviewList.isEmpty()) mySections.put("Overview", overviewList);
                List<Updates> lethalityList = new ArrayList<>();
                for (String f : mutLethalityFields) {
                    List<Updates> u = perField.get(f);
                    if (u != null) lethalityList.addAll(u);
                }
                if (!lethalityList.isEmpty()) mySections.put("Lethality", lethalityList);
                if (mut.getGenes() != null) {
                    List<Updates> genesList = new ArrayList<>();
                    for (org.zfin.zirc.entity.Gene g : mut.getGenes()) {
                        Map<String, List<Updates>> byField = geneFieldUpdates.get(g.getId());
                        if (byField != null) for (List<Updates> u : byField.values()) genesList.addAll(u);
                    }
                    if (!genesList.isEmpty()) mySections.put("Genes", genesList);
                }
                if (mut.getLesions() != null) {
                    List<Updates> lesionsList = new ArrayList<>();
                    for (org.zfin.zirc.entity.Lesion lz : mut.getLesions()) {
                        Map<String, List<Updates>> byField = lesionFieldUpdates.get(lz.getId());
                        if (byField != null) for (List<Updates> u : byField.values()) lesionsList.addAll(u);
                    }
                    if (!lesionsList.isEmpty()) mySections.put("Lesions", lesionsList);
                }
                if (mut.getGenotypingAssays() != null) {
                    List<Updates> assaysList = new ArrayList<>();
                    for (org.zfin.zirc.entity.GenotypingAssay ga : mut.getGenotypingAssays()) {
                        Map<String, List<Updates>> byField = assayFieldUpdates.get(ga.getId());
                        if (byField != null) for (List<Updates> u : byField.values()) assaysList.addAll(u);
                    }
                    if (!assaysList.isEmpty()) mySections.put("Genotyping Assays", assaysList);
                }
                if (mut.getPhenotypes() != null) {
                    List<Updates> phenoList = new ArrayList<>();
                    for (org.zfin.zirc.entity.Phenotype p : mut.getPhenotypes()) {
                        Map<String, List<Updates>> byField = phenotypeFieldUpdates.get(p.getId());
                        if (byField != null) for (List<Updates> u : byField.values()) phenoList.addAll(u);
                    }
                    if (!phenoList.isEmpty()) mySections.put("Phenotypes", phenoList);
                }
                for (List<Updates> lst : mySections.values()) {
                    lst.sort((a, b) -> b.getWhenUpdated().compareTo(a.getWhenUpdated()));
                }
                mutationSectionUpdates.put(mid, mySections);
            }
        }
        // Per-mutation total roll-up (union of all this mutation's sections).
        Map<Long, List<Updates>> mutationAllUpdates = new LinkedHashMap<>();
        for (Map.Entry<Long, Map<String, List<Updates>>> e : mutationSectionUpdates.entrySet()) {
            List<Updates> all = new ArrayList<>();
            for (List<Updates> lst : e.getValue().values()) {
                all.addAll(lst);
            }
            all.sort((a, b) -> b.getWhenUpdated().compareTo(a.getWhenUpdated()));
            if (!all.isEmpty()) {
                mutationAllUpdates.put(e.getKey(), all);
            }
        }
        model.addAttribute("mutationSectionUpdates", mutationSectionUpdates);
        model.addAttribute("mutationAllUpdates", mutationAllUpdates);
        // Whole-submission roll-up: every audited change anywhere on this
        // submission (already DESC-sorted by the query above).
        model.addAttribute("submissionAllUpdates", allUpdates);

        model.addAttribute("mutationStatus", mutationStatus);
        model.addAttribute("mutationFieldStatus", mutationFieldStatus);
        model.addAttribute("mutationSectionStatus", mutationSectionStatus);
        model.addAttribute("geneFieldStatus", geneFieldStatus);
        model.addAttribute("lesionFieldStatus", lesionFieldStatus);
        model.addAttribute("assayFieldStatus", assayFieldStatus);
        model.addAttribute("phenotypeFieldStatus", phenotypeFieldStatus);
        if (!mutationLabels.isEmpty()) {
            model.addAttribute("subSections", Map.of("Mutations", mutationLabels));
            model.addAttribute("subSectionStatus", subSectionStatus);
            model.addAttribute("subSubSections", subSubSections);
            model.addAttribute("subSubSectionStatus", subSubSectionStatus);
        }

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

    // ─────────────────────────────────────────────────────────────────────
    // Mutations: editor sub-page + per-field save + add/remove.
    // The parent line submission's edit page links here; mutations get their
    // own URL because the per-mutation field set is large enough that
    // expanding inline on the parent would be unwieldy.
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Create-and-redirect: persists a fresh empty mutation under {@code lsId}
     * (creating the parent submission too if it doesn't exist yet, just like
     * {@link #patchLineSubmission}), then sends the user to the mutation editor.
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

    /**
     * Companion to {@link #newMutation} for the "New Line Submission" flow,
     * where the parent submission doesn't exist yet. Creates an empty
     * submission AND an empty mutation under it in one transaction, then
     * redirects to the mutation editor. The mutation's "Back to Submission"
     * link lets the curator fill in the parent submission's fields after.
     */
    @GetMapping("/line-submission/new-with-mutation")
    public String newSubmissionWithMutation() {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        try {
            Mutation m = lineSubmissionService.addMutation(null, currentUser);
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
     * Upload a file attached to a genotyping assay. {@code kind} categorizes
     * the upload (chromatogram / gel_image / result_image / melt_curve)
     * per the xlsx field matrix. The server writes the bytes under
     * {@code $TARGETROOT/server_apps/data_transfer/ZIRC/<submission>/}
     * and returns the parent assay's mutation DTO so the React form can
     * refresh its file list.
     */
    @PostMapping("/assay/{assayId}/file")
    @ResponseBody
    public MutationDTO uploadAssayFile(@PathVariable Long assayId,
                                       @RequestParam("kind") String kind,
                                       @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        try {
            org.zfin.zirc.entity.GenotypingAssayFile saved =
                    lineSubmissionService.addAssayFile(assayId, kind, file, currentUser);
            HibernateUtil.flushAndCommitCurrentSession();
            return MutationDTO.from(saved.getAssay().getMutation());
        } catch (java.io.IOException e) {
            HibernateUtil.rollbackTransaction();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to store uploaded file: " + e.getMessage());
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Stream a previously uploaded file's bytes back to the client. Used
     * for the per-row "download" link next to each attachment.
     */
    @GetMapping("/assay-file/{fileId}/download")
    public void downloadAssayFile(@PathVariable Long fileId,
                                  jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        HibernateUtil.createTransaction();
        org.zfin.zirc.entity.GenotypingAssayFile f;
        try {
            f = lineSubmissionService.requireAssayFile(fileId);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
        java.io.File onDisk = lineSubmissionService.absoluteFilePath(f);
        if (!onDisk.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "File missing from disk: " + onDisk.getPath());
        }
        // Force a non-rendering content type. The persisted f.getContentType()
        // was taken verbatim from the upload's multipart Content-Type, which
        // a malicious uploader controls. Even with Content-Disposition:
        // attachment, some clients (Safari, embed elements, mis-configured
        // proxies) honour the original type — echoing it could enable
        // XSS-on-download. application/octet-stream forces a download path
        // regardless. Curators rarely need inline render; they download to
        // a known location and open with the right local viewer.
        response.setContentType("application/octet-stream");
        // Strip CR/LF/control characters and double quotes from the
        // filename before writing it into a response header — otherwise
        // a curator-supplied filename could split the header (CRLF
        // injection). The fallback to "file" handles a sanitized
        // name that's empty after stripping.
        String safeFilename = f.getOriginalFilename()
                .replaceAll("[\\p{Cntrl}\"]", "_");
        if (safeFilename.isBlank()) {
            safeFilename = "file";
        }
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + safeFilename + "\"");
        response.setContentLengthLong(onDisk.length());
        try (var in = new java.io.FileInputStream(onDisk)) {
            in.transferTo(response.getOutputStream());
        }
    }

    /** Remove an uploaded file (DB row + disk file). Returns the parent
     *  mutation DTO so the React form can refresh. */
    @DeleteMapping("/assay-file/{fileId}")
    @ResponseBody
    public MutationDTO deleteAssayFile(@PathVariable Long fileId) {
        HibernateUtil.createTransaction();
        try {
            org.zfin.zirc.entity.GenotypingAssayFile f = lineSubmissionService.requireAssayFile(fileId);
            Mutation parent = f.getAssay().getMutation();
            lineSubmissionService.removeAssayFile(fileId);
            HibernateUtil.flushAndCommitCurrentSession();
            return MutationDTO.from(parent);
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
    public List<Map<String, String>> searchMarkers(
            @RequestParam(value = "term", required = false) String term,
            @RequestParam(value = "typeGroup", required = false) String typeGroup) {
        if (term == null || term.isBlank()) {
            return Collections.emptyList();
        }
        // typeGroup narrows results to a Marker.TypeGroup (e.g. GENEDOM,
        // SSLP). Compared against MarkerType.typeGroupStrings — the
        // typed enum-Set property is @Transient and unqueryable, but
        // the underlying join table behind typeGroupStrings is fully
        // mapped. Validate via the enum to reject random input and let
        // bad group names quietly return [] (the dropdown stays quiet
        // rather than 4xxing mid-type).
        String groupString = null;
        if (typeGroup != null && !typeGroup.isBlank()) {
            try {
                groupString = org.zfin.marker.Marker.TypeGroup.valueOf(typeGroup).name();
            } catch (IllegalArgumentException e) {
                return Collections.emptyList();
            }
        }
        String hql = groupString == null
                ? "from Marker where lower(abbreviation) like :q order by abbreviation"
                : "select m from Marker m join m.markerType.typeGroupStrings tg"
                        + " where lower(m.abbreviation) like :q and tg = :group"
                        + " order by m.abbreviation";
        var query = HibernateUtil.currentSession()
                .createQuery(hql, org.zfin.marker.Marker.class)
                .setParameter("q", "%" + term.toLowerCase() + "%");
        if (groupString != null) {
            query.setParameter("group", groupString);
        }
        List<org.zfin.marker.Marker> markers = query.setMaxResults(20).list();
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
     * Feature (allele) autocomplete. Backs the mutation editor's
     * "Allele Designation" field when "exists in ZFIN" is checked.
     * Searches the Feature table by abbreviation prefix or substring.
     * Same wire shape as {@link #searchMarkers}.
     */
    @GetMapping("/features/search")
    @ResponseBody
    public List<Map<String, String>> searchFeatures(@RequestParam(value = "term", required = false) String term) {
        if (term == null || term.isBlank()) {
            return Collections.emptyList();
        }
        List<org.zfin.feature.Feature> features = HibernateUtil.currentSession()
                .createQuery(
                        "from Feature where lower(abbreviation) like :q order by abbreviationOrder",
                        org.zfin.feature.Feature.class)
                .setParameter("q", "%" + term.toLowerCase() + "%")
                .setMaxResults(20)
                .list();
        List<Map<String, String>> out = new ArrayList<>();
        for (org.zfin.feature.Feature f : features) {
            Map<String, String> entry = new HashMap<>();
            entry.put("label", f.getAbbreviation() + " (" + f.getZdbID() + ")");
            entry.put("value", f.getAbbreviation());
            entry.put("zdbID", f.getZdbID());
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

    // ─────────────────────────────────────────────────────────────────────
    // Unified patch endpoints.
    //
    // One endpoint per editor (line submission / mutation) that accepts
    // {path, value} JSON and routes to the right save logic. Added for
    // the schema-driven React renderer (ZFIN-10265) which commits one
    // path at a time. The legacy per-section endpoints
    // (save-field, save-reasons, save-linked-features, save-genes, ...)
    // still work; React migrates off them in a follow-up.
    // ─────────────────────────────────────────────────────────────────────

    public static class PatchRequest {
        public String zdbID;     // optional for line-submission patch (new-flow create-on-first-save)
        public String path;
        public JsonNode value;
    }

    @PostMapping(value = "/line-submission/patch", consumes = "application/json")
    @ResponseBody
    public LineSubmissionDTO patchLineSubmission(@RequestBody PatchRequest req) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        try {
            LineSubmission submission = lineSubmissionService.applyPatch(
                    req.zdbID, req.path, req.value, currentUser);
            HibernateUtil.flushAndCommitCurrentSession();
            return LineSubmissionDTO.from(submission);
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }

    @PostMapping(value = "/mutation/{mutationId}/patch", consumes = "application/json")
    @ResponseBody
    public MutationDTO patchMutation(@PathVariable Long mutationId, @RequestBody PatchRequest req) {
        Person currentUser = ProfileService.getCurrentSecurityUser();
        HibernateUtil.createTransaction();
        try {
            Mutation m = lineSubmissionService.applyMutationPatch(
                    mutationId, req.path, req.value, currentUser);
            HibernateUtil.flushAndCommitCurrentSession();
            return MutationDTO.from(m);
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        }
    }
}
