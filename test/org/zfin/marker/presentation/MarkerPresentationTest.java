package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAbbreviationComparator;
import org.zfin.marker.MarkerType;

import java.util.*;

import static org.junit.Assert.*;

public class MarkerPresentationTest {

    private Marker marker;

    private Logger logger = Logger.getLogger(MarkerPresentationTest.class) ;

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
        assertEquals("<a href=\"/action/marker/view/ZDB-GENE-081507-1\" name=\"fibroblast growth factor 8 a\" id='ZDB-GENE-081507-1'>" +
                "<span class=\"genedom\" title=\"fibroblast growth factor 8 a\">fgf8</span></a>" ,
                link
        );

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

    @Test
    public void markerAbbreviationComparator(){
        Marker m1 = new Marker() ;
        m1.setAbbreviation("abcd10");
        m1.setAbbreviationOrder("abcd10");

        Marker m2 = new Marker() ;
        m2.setAbbreviation("abcd1");
        m2.setAbbreviationOrder("abcd01");

        List<Marker> markers = new ArrayList<Marker>();
        markers.add(m1) ;
        markers.add(m2) ;

        Collections.sort(markers,new MarkerAbbreviationComparator("test"));
        assertEquals("abcd1",markers.get(0).getAbbreviation()) ;
        assertEquals("abcd10",markers.get(1).getAbbreviation()) ;

        m2.setAbbreviation("test3") ;
        m2.setAbbreviationOrder("test03") ;

        Collections.sort(markers,new MarkerAbbreviationComparator("test"));
        assertEquals("test3",markers.get(0).getAbbreviation()) ;
        assertEquals("abcd10",markers.get(1).getAbbreviation()) ;

        m1.setAbbreviation("test10") ;
        m1.setAbbreviationOrder("test10") ;

        Collections.sort(markers,new MarkerAbbreviationComparator("test"));
        assertEquals("test3",markers.get(0).getAbbreviation()) ;
        assertEquals("test10",markers.get(1).getAbbreviation()) ;

        Collections.sort(markers,new MarkerAbbreviationComparator("test1"));
        assertEquals("test10",markers.get(0).getAbbreviation()) ;
        assertEquals("test3",markers.get(1).getAbbreviation()) ;

        Collections.sort(markers,new MarkerAbbreviationComparator("abcd"));
        assertEquals("test3",markers.get(0).getAbbreviation()) ;
        assertEquals("test10",markers.get(1).getAbbreviation()) ;

        m1.setAbbreviation("abcd10");
        m1.setAbbreviationOrder("abcd10");

        Collections.sort(markers,new MarkerAbbreviationComparator("abcd"));
        assertEquals("abcd10",markers.get(0).getAbbreviation()) ;
        assertEquals("test3",markers.get(1).getAbbreviation()) ;
    }

    private List<PreviousNameLight> createPreviousNames(String realName,String... names){
        List<PreviousNameLight> list = new ArrayList<PreviousNameLight>();

        for(String alias : names){
            PreviousNameLight previousNameLight = new PreviousNameLight(realName);
            previousNameLight.setAlias(alias);
            list.add(previousNameLight);
        }

        return list ;
    }

    // From case 6959, sorty by citation, informative name (no colons), and finally alphabetical
    @Test
    public void testPreviousNameSort(){

        assertTrue(createPreviousNames("myod1","wu:fb57a01").get(0).isUninformative());
        assertTrue(createPreviousNames("myod1","zgc:136744").get(0).isUninformative());
        assertFalse(createPreviousNames("pax6a", "pax[zf-a]").get(0).isUninformative());
//        logger.info(StringUtils.getLevenshteinDistance("pax6a", "pax[zf-a]"));

        List<PreviousNameLight> list2 = createPreviousNames(
                "myod1", // real name
                "MyoD",
                "myod",
                "MyoD1",
                "etID309723.25",
                "wu:fb57a01",
                "zgc:136744"
        );
        Collections.sort(list2);

        int i ;
        i = 0 ;
        assertEquals("MyoD",list2.get(i++).getAlias());
        assertEquals("myod",list2.get(i++).getAlias());
        assertEquals("MyoD1",list2.get(i++).getAlias());
        assertEquals("etID309723.25",list2.get(i++).getAlias()); // TODO: should not be here
        assertEquals("wu:fb57a01",list2.get(i++).getAlias());
        assertEquals("zgc:136744",list2.get(i++).getAlias());

        // for pax6a
        // ceri's match: pax6, paxzfa, pax-a, Pax6.1, pax[zf-a](1),zfpax-6a, zfpax-6b, cb280(1), etID309716.25(1), fc20e07, wu:fc20e07(1),

        // my match: pax6, paxzfa, pax-a, Pax6.1, pax[zf-a](1),zfpax-6a, zfpax-6b, cb280(1), etID309716.25(1), fc20e07, wu:fc20e07(1),

        // test to make sure that the proper of these are informative

//        assertEquals();

        List<PreviousNameLight> list = createPreviousNames(
                "pax6a", // real name
                "pax6",
                "paxzfa",
                "pax-a",
                "Pax6.1",
                "pax[zf-a]",
                "zfpax-6a",
                "zfpax-6b",
                "cb280",
                "etID309716.25",
                "fc20e07",
                "wu:fc20e07"
        );

        Collections.sort(list);
        i = 0 ;
        assertEquals("pax-a",list.get(i++).getAlias());
        assertEquals("pax6",list.get(i++).getAlias());
        assertEquals("Pax6.1",list.get(i++).getAlias());
        assertEquals("pax[zf-a]",list.get(i++).getAlias());
        assertEquals("paxzfa",list.get(i++).getAlias());
        assertEquals("zfpax-6a",list.get(i++).getAlias());
        assertEquals("zfpax-6b",list.get(i++).getAlias());
        assertEquals("cb280",list.get(i++).getAlias());
        assertEquals("etID309716.25",list.get(i++).getAlias());
        assertEquals("fc20e07",list.get(i++).getAlias());
        assertEquals("wu:fc20e07",list.get(i++).getAlias());

    }


    @Test
    public void getTypeFromZdbID(){
        MarkerViewController markerViewController = new MarkerViewController();
        assertEquals("GENE",markerViewController.getTypeForZdbID("ZDB-GENE-980526-403"));
        assertEquals("BAC_END",markerViewController.getTypeForZdbID("ZDB-BAC_END-011115-15"));
    }


}
