package org.zfin.sequence.reno.presentation;

import org.junit.Before;
import org.junit.Test;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.framework.presentation.RunCandidatePresentation;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.Query;
import org.zfin.sequence.reno.Candidate;
import org.zfin.sequence.reno.RunCandidate;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

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
        Set<Marker.TypeGroup> groups = new HashSet<>();
        groups.add(Marker.TypeGroup.GENEDOM);
        type.setTypeGroups(groups);
        marker.setMarkerType(type);

        runCandidate = new RunCandidate();
        Candidate candidate = new Candidate() ;
        runCandidate.setCandidate(candidate);


        MarkerDBLink dblink = new MarkerDBLink();
        dblink.setMarker(marker);

        ReferenceDatabase refDb = new ReferenceDatabase();
        ForeignDBDataType dataType = new ForeignDBDataType();
        dataType.setSuperType(ForeignDBDataType.SuperType.SEQUENCE);
        dataType.setDataType(ForeignDBDataType.DataType.POLYPEPTIDE);
        refDb.setForeignDBDataType(dataType);
        refDb.setZdbID("ZDB-TEMPTESTID-123456-1");
        dblink.setReferenceDatabase(refDb);

        Set<MarkerDBLink> dblinks = new HashSet<>();
        dblinks.add(dblink);

        Accession acc = new Accession();
        acc.setDbLinks(dblinks.stream().map(link -> (DBLink) link).collect(Collectors.toSet()));

        Query query = new Query();
        query.setAccession(acc);

        Set<Query> queries = new HashSet<>();
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
        assertEquals("<a href=\"/ZDB-GENE-081507-1\" id=\"ZDB-GENE-081507-1\" title=\"fibroblast growth factor 8 a\"><span class=\"genedom\" title=\"fibroblast growth factor 8 a\" id=\"Gene Symbol\">fgf8</span></a>"
                ,link
        );
    }

    /**
     * Create a span-tag for runCandidate with no suggested name.
     */
    @Test
    public void candidateName() {
        String name = RunCandidatePresentation.getName(runCandidate);
        assertEquals("Span tag", "<span class=\"genedom\" title=\"fibroblast growth factor 8 a\" id=\"Gene Symbol\">fgf8</span>", name);
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
