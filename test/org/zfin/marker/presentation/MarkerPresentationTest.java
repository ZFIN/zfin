package org.zfin.marker.presentation;

import org.junit.Before;
import org.junit.Test;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MarkerPresentationTest {

    private Marker marker;

    @Before
    public void setUp() {
        marker = new Marker();
        marker.setZdbID("ZDB-GENE-081507-1");
        marker.setAbbreviation("fgf8");
        marker.setName("fibroblast growth factor 8 a");
        MarkerType type = new MarkerType();
        type.setType(Marker.Type.GENE);
        Set<Marker.TypeGroup> groups = new HashSet<Marker.TypeGroup>();
        groups.add(Marker.TypeGroup.GENEDOM);
        type.setTypeGroups(groups);
        marker.setMarkerType(type);
    }

    /**
     * Create a marker hyperlink with the zdbID in the URL and
     * a marker specific span-tag including style sheet.
     */
    @Test
    public void markerLink() {
        String link = MarkerPresentation.getLink(marker);
	    assertTrue(link.endsWith("MIval=aa-markerview.apg&OID=ZDB-GENE-081507-1\" id='ZDB-GENE-081507-1'>" +
                "<span class=\"genedom\" title=\"fibroblast growth factor 8 a\">fgf8</span></a>"));

    }


    /**
     * Create a span-tag for marker.
     */
    @Test
    public void markerName() {
        String name = MarkerPresentation.getName(marker);
        assertEquals("Span tag", "<span class=\"genedom\" title=\"fgf8\">fibroblast growth factor 8 a</span>", name);
    }

    /**
     * Create a span-tag for marker.
     */
    @Test
    public void markerAbbreviation() {
        String abbreviation = MarkerPresentation.getAbbreviation(marker);
        assertEquals("Span tag", "<span class=\"genedom\" title=\"fibroblast growth factor 8 a\">fgf8</span>", abbreviation);
    }

}