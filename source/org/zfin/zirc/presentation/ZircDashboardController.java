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

        // Per-section approval state. Loaded for the submission + each
        // mutation so the JSP can render initial checkbox state on every
        // section header. Key format: "<recId>|<sectionName>".
        List<Object[]> approvalRows = HibernateUtil.currentSession()
                .createQuery(
                    "select recId, sectionName, approved from ZircLineSubmissionSectionApproval "
                  + "where recId in :rids",
                    Object[].class)
                .setParameterList("rids", auditKeys)
                .list();
        Map<String, Boolean> sectionApprovals = new LinkedHashMap<>();
        for (Object[] r : approvalRows) {
            sectionApprovals.put(r[0] + "|" + r[1], (Boolean) r[2]);
        }
        model.addAttribute("sectionApprovals", sectionApprovals);

        // Overlay APPROVED onto the section status maps so the rollup
        // badges reflect curator approval. The slug-to-label mapping
        // mirrors the JSP's section names.
        Map<String, String> SLUG_TO_SECTION = Map.ofEntries(
                Map.entry("overview",          "Overview"),
                Map.entry("mutations",         "Mutations"),
                Map.entry("linked-features",   "Linked Features"),
                Map.entry("background",        "Background"),
                Map.entry("additional-info",   "Additional Info"),
                Map.entry("genes",             "Genes"),
                Map.entry("lesions",           "Lesions"),
                Map.entry("genotyping-assays", "Genotyping Assays"),
                Map.entry("phenotypes",        "Phenotypes"),
                Map.entry("lethality",         "Lethality"),
                Map.entry("publications",      "Publications"));
        for (Map.Entry<String, Boolean> e : sectionApprovals.entrySet()) {
            if (!Boolean.TRUE.equals(e.getValue())) continue;
            String[] parts = e.getKey().split("\\|", 2);
            if (parts.length != 2) continue;
            String recIdKey  = parts[0];
            String slug      = parts[1];
            String sectionLabel = SLUG_TO_SECTION.get(slug);
            if (submission.getZdbID().equals(recIdKey)) {
                if (sectionLabel != null) {
                    status.bySection().put(sectionLabel, FieldStatus.APPROVED);
                }
            } else if (recIdKey.startsWith("ZIRC-MUT-")) {
                Long mid;
                try { mid = Long.parseLong(recIdKey.substring("ZIRC-MUT-".length())); }
                catch (NumberFormatException nfe) { continue; }
                if ("mutation".equals(slug)) {
                    // The mutation header itself
                    mutationStatus.put(mid, FieldStatus.APPROVED);
                } else if (sectionLabel != null) {
                    Map<String, FieldStatus> bySect = mutationSectionStatus.get(mid);
                    if (bySect != null) bySect.put(sectionLabel, FieldStatus.APPROVED);
                }
            }
        }
        // Re-roll the parent statuses upward so a section reads APPROVED
        // when all its children are. mutation overall = worst-of inner
        // sections; "Mutations" = worst-of all mutations (or MISSING when
        // none exist); overallStatus = worst-of all top-level sections.
        // An explicit approval on the parent itself never gets downgraded.
        for (Map.Entry<Long, Map<String, FieldStatus>> e : mutationSectionStatus.entrySet()) {
            FieldStatus worst = FieldStatus.APPROVED;
            for (FieldStatus s : e.getValue().values()) {
                if (s != null) worst = worst.worse(s);
            }
            if (Boolean.TRUE.equals(sectionApprovals.get("ZIRC-MUT-" + e.getKey() + "|mutation"))) {
                worst = FieldStatus.APPROVED;
            }
            mutationStatus.put(e.getKey(), worst);
        }
        // Recompute top-level "Mutations": presence-check + worst-of-mutations.
        FieldStatus mutsRollup;
        if (mutationStatus.isEmpty()) {
            mutsRollup = FieldStatus.MISSING;
        } else {
            mutsRollup = FieldStatus.APPROVED;
            for (FieldStatus s : mutationStatus.values()) {
                if (s != null) mutsRollup = mutsRollup.worse(s);
            }
        }
        if (Boolean.TRUE.equals(sectionApprovals.get(submission.getZdbID() + "|mutations"))) {
            mutsRollup = FieldStatus.APPROVED;
        }
        status.bySection().put("Mutations", mutsRollup);

        // Refresh subSectionStatus from the (now potentially-approved)
        // mutationStatus map so the left-nav "Mutation N" entries pick up
        // approved coloring too.
        if (submission.getMutations() != null) {
            int i = 1;
            for (Mutation mut : submission.getMutations()) {
                String label = "Mutation " + i++;
                FieldStatus st = mutationStatus.get(mut.getId());
                if (st != null) subSectionStatus.put(label, st);
            }
        }

        // Recompute page-overall from the (now potentially-approved) top-level sections.
        FieldStatus pageOverall = FieldStatus.APPROVED;
        for (FieldStatus s : status.bySection().values()) {
            if (s != null) pageOverall = pageOverall.worse(s);
        }
        model.addAttribute("overallStatus", pageOverall);

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
    /**
     * Schema-driven read-only detail view. Reuses the existing
     * {@link LineSubmissionStatusComputer} maps and the central
     * {@code Updates} table; serializes both into a single JSON blob that
     * the React mount reads off the DOM.
     *
     * <p>Smallest-cutover scope: submission-level field status, section
     * status, field updates, section updates. Per-mutation status maps,
     * gene/lesion/assay/phenotype status, and the dashboard's status-
     * overview bar are intentionally NOT computed here — they ride on a
     * follow-up port and the legacy {@link #viewLineSubmission} JSP route
     * remains in service as the off-ramp.
     */
    @GetMapping("/line-submission/{zdbID}/detail-react")
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

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("fieldStatus",     submissionFieldStatus);
        payload.put("sectionStatus",   submissionSectionStatus);
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
