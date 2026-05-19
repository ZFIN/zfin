package org.zfin.datatransfer.go.service;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for the GafEntry / MarkerGoTermEvidence toString parser used by the
 * Matching errors table in the GAF report.
 */
public class GafReportBuilderTest {

    @Test
    public void extractsFieldsFromGafEntryBlob() {
        Map<String, String> f = GafReportBuilder.parseEntryFields(
            "Goref ID is not known or loaded[GO_REF:0000115]:\n" +
            "GafEntry{entryId='URS00003B6A21_7955', qualifier='involved_in', goid='GO:0035195'," +
            " pmid='GO_REF:0000115', evidenceCode='IEA', inferences='Rfam:RF00256'," +
            " taxonID='taxon:7955', createdDate='20260428', createdBy='RNAcentral'," +
            " annotExtn='', geneProducFormID=''}");

        assertEquals("URS00003B6A21_7955", f.get("entryId"));
        assertEquals("involved_in",        f.get("qualifier"));
        assertEquals("GO:0035195",         f.get("goid"));
        assertEquals("GO_REF:0000115",     f.get("pmid"));
        assertEquals("IEA",                f.get("evidenceCode"));
        assertEquals("RNAcentral",         f.get("createdBy"));
    }

    @Test
    public void extractsFieldsFromMarkerGoTermEvidenceBlob() {
        Map<String, String> f = GafReportBuilder.parseEntryFields(
            "Failed to add batch:\n" +
            "MarkerGoTermEvidence{zdbID='ZDB-MRKRGOEV-221010-21085', marker='dnajb5'," +
            " evidenceCode='inferred from biological aspect of ancestor', flag='null '," +
            " qualifierRelation='enables', source='ZDB-PUB-110330-1'," +
            " goTerm='obsolete unfolded protein binding', organizationCreatedBy=GO_Central}");

        assertEquals("ZDB-MRKRGOEV-221010-21085",         f.get("zdbID"));
        assertEquals("dnajb5",                            f.get("marker"));
        assertEquals("enables",                           f.get("qualifierRelation"));
        assertEquals("obsolete unfolded protein binding", f.get("goTerm"));
        assertEquals("ZDB-PUB-110330-1",                  f.get("source"));
    }

    @Test
    public void returnsEmptyMapWhenNeitherShapeIsPresent() {
        assertTrue(GafReportBuilder.parseEntryFields("Just a plain error message").isEmpty());
        assertTrue(GafReportBuilder.parseEntryFields("").isEmpty());
    }

    @Test
    public void embeddedApostropheInValueTruncatesField() {
        // Locks in the current behaviour so a future toString that uses
        // single-quoted values with embedded apostrophes doesn't silently regress.
        Map<String, String> f = GafReportBuilder.parseEntryFields(
            "GafEntry{name='O'Brien', qualifier='enables'}");
        // name terminates at the first inner apostrophe; the regex resumes at the
        // next k='v' so qualifier is still picked up correctly. Documented edge.
        assertEquals("O",       f.get("name"));
        assertEquals("enables", f.get("qualifier"));
    }
}
