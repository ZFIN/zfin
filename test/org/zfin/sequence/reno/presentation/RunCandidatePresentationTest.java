package org.zfin.sequence.reno.presentation;

import org.junit.Before;
import org.junit.Test;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.framework.presentation.RunCandidatePresentation;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.sequence.Accession;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.blast.Query;
import org.zfin.sequence.reno.Candidate;
import org.zfin.sequence.reno.RunCandidate;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 */
public class RunCandidatePresentationTest extends EntityPresentation {

    private RunCandidate runCandidate;

    @Before
    public void setUp() {
        Marker marker = new Marker();
        marker.setZdbID("ZDB-GENE-081507-1");
        marker.setAbbreviation("fgf8");
        marker.setName("fibroblast growth factor 8 a");
        MarkerType type = new MarkerType();
        type.setType(Marker.Type.GENE);
        Set<Marker.TypeGroup> groups = new HashSet<Marker.TypeGroup>();
        groups.add(Marker.TypeGroup.GENEDOM);
        type.setTypeGroups(groups);
        marker.setMarkerType(type);

        runCandidate = new RunCandidate();
        Candidate candidate = new Candidate() ;
        runCandidate.setCandidate(candidate);


        MarkerDBLink dblink = new MarkerDBLink();
        dblink.setMarker(marker);
        
        Set<MarkerDBLink> dblinks = new HashSet<MarkerDBLink>();
        dblinks.add(dblink);

        Accession acc = new Accession();
        acc.setBlastableMarkerDBLinks(dblinks);

        Query query = new Query();
        query.setAccession(acc);

        Set<Query> queries = new HashSet<Query>();
        queries.add(query);
        
        runCandidate.setCandidateQueries(queries);

    }

    /**
     * Create a runCandidate hyperlink with the zdbID in the URL and
     * a run specific span-tag including style sheet.
     * The Candidate has no suggested name.
     */
    @Test
    public void candidateLink() {
        String link = RunCandidatePresentation.getLink(runCandidate);
        assertTrue(link.endsWith("?MIval=aa-markerview.apg&OID=ZDB-GENE-081507-1\" id='ZDB-GENE-081507-1'>" +
                "<span class=\"genedom\" title=\"fibroblast growth factor 8 a\">fgf8</span></a>"));
    }

    /**
     * Create a span-tag for runCandidate with no suggested name.
     */
    @Test
    public void candidateName() {
        String name = RunCandidatePresentation.getName(runCandidate);
        assertEquals("Span tag", "<span class=\"genedom\" title=\"fibroblast growth factor 8 a\">fgf8</span>", name);
    }

    /**
     * Create a runCandidate hyperlink with the zdbID in the URL and
     * a run specific span-tag including style sheet.
     * The Candidate has no identified marker.
     */
    @Test
    public void candidateLinkWithSuggestedName() {
        runCandidate.getCandidate().setSuggestedName("Harry");
        String link = RunCandidatePresentation.getLink(runCandidate);
        assertEquals("Hyperlink", "Harry", link);
    }

    /**
     * Create a span-tag for runCandidate with an identified marker.
     */
    @Test
    public void candidateNameWithSuggestedName() {
        runCandidate.getCandidate().setSuggestedName("Harvey");
        String name = RunCandidatePresentation.getName(runCandidate);
        assertEquals("Span tag", "Harvey", name);
    }


}
