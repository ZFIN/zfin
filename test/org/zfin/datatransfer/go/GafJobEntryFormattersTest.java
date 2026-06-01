package org.zfin.datatransfer.go;

import org.junit.Test;
import org.zfin.mutant.MarkerGoTermAnnotationExtn;
import org.zfin.mutant.MarkerGoTermAnnotationExtnGroup;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.InferenceGroupMember;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Pure unit tests for the static formatters on {@link GafJobEntry} that
 * derive GAF column 8 / 16 strings from {@link MarkerGoTermEvidence}. These
 * are the new GAF report columns added for Noctua-style curation visibility.
 *
 * <p>Annotation-extension relation name resolution goes through the ontology
 * repository, which needs Hibernate at runtime. The formatter is wrapped in a
 * try/catch so the raw relationshipTerm ZDB ID is kept as a fallback; these
 * tests exercise that fallback path so they don't depend on the DB.
 */
public class GafJobEntryFormattersTest {

    @Test
    public void withFromIsEmptyForNoInferences() {
        MarkerGoTermEvidence m = new MarkerGoTermEvidence();
        assertEquals("", GafJobEntry.formatWithFrom(m));
    }

    @Test
    public void withFromJoinsSortedPipeDelimited() {
        MarkerGoTermEvidence m = new MarkerGoTermEvidence();
        Set<InferenceGroupMember> inferences = new HashSet<>();
        inferences.add(makeInference("UniProtKB:P12345"));
        inferences.add(makeInference("Rfam:RF00256"));
        inferences.add(makeInference("UniProtKB:P00001"));
        m.setInferredFrom(inferences);

        // Sorted lexically so the output is stable regardless of Set iteration order.
        assertEquals("Rfam:RF00256|UniProtKB:P00001|UniProtKB:P12345",
            GafJobEntry.formatWithFrom(m));
    }

    @Test
    public void annotationExtensionsAreEmptyForNoGroups() {
        MarkerGoTermEvidence m = new MarkerGoTermEvidence();
        assertEquals("", GafJobEntry.formatAnnotationExtensions(m));
    }

    @Test
    public void annotationExtensionsFallBackToRelationZdbIdWithoutDb() {
        // No Hibernate session is available in unit tests, so the lookup
        // throws and the formatter keeps the raw ZDB ID. This locks in the
        // never-silently-empty contract.
        MarkerGoTermEvidence m = new MarkerGoTermEvidence();
        Set<MarkerGoTermAnnotationExtnGroup> groups = new HashSet<>();
        MarkerGoTermAnnotationExtnGroup group = new MarkerGoTermAnnotationExtnGroup();
        Set<MarkerGoTermAnnotationExtn> extns = new HashSet<>();
        extns.add(new MarkerGoTermAnnotationExtn("ZDB-TERM-RELATION-part-of", "GO:0005634"));
        extns.add(new MarkerGoTermAnnotationExtn("ZDB-TERM-RELATION-occurs-in", "CL:0000540"));
        group.setMgtAnnoExtns(extns);
        groups.add(group);
        m.setGoTermAnnotationExtnGroup(groups);

        String out = GafJobEntry.formatAnnotationExtensions(m);

        // Output sorted lexically and pipe-joined; both entries present.
        assertTrue("expected sorted output starting with occurs-in, got: " + out,
            out.startsWith("ZDB-TERM-RELATION-occurs-in(CL:0000540)"));
        assertTrue("expected both entries pipe-joined, got: " + out,
            out.contains("|ZDB-TERM-RELATION-part-of(GO:0005634)"));
    }

    private static InferenceGroupMember makeInference(String inferredFrom) {
        InferenceGroupMember member = new InferenceGroupMember();
        member.setInferredFrom(inferredFrom);
        return member;
    }
}
