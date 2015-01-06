package org.zfin.framework.presentation;

import org.junit.Test;
import org.zfin.ontology.presentation.OntologyBean;

import java.util.EnumMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Class used for showing and hiding sections on a detail page.
 * By default each section is set to hide.
 */
public class SectionVisibilityTest {

    @Test
    public void simpleSectionVisibilityDefaultFalse() {
        SectionVisibility vis = new SectionVisibility<>(OntologyBean.Section.class);
        EnumMap<OntologyBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        int numberOfSections = OntologyBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        for (boolean show : sections.values()) {
            assertTrue(!show);
        }
        assertTrue(vis.isAllSectionsInvisible());

        sections = vis.getSectionDataExistence();
        assertTrue(sections != null);
        numberOfSections = OntologyBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        for (boolean show : sections.values()) {
            assertTrue(!show);
        }
    }

    @Test
    public void simpleSectionVisibilityDefaultTrue() {
        SectionVisibility vis = new SectionVisibility<>(OntologyBean.Section.class, true);
        EnumMap<OntologyBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        int numberOfSections = OntologyBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        for (boolean show : sections.values()) {
            assertTrue(show);
        }
        assertTrue(vis.isAllSectionsVisible());

        sections = vis.getSectionDataExistence();
        assertTrue(sections != null);
        numberOfSections = OntologyBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        for (boolean show : sections.values()) {
            assertTrue(show);
        }
    }

    /**
     * Set default false visibilities all to true manually
     */
    @Test
    public void setVisibilityToTrueManually() {
        SectionVisibility vis = new SectionVisibility<>(OntologyBean.Section.class, false);
        EnumMap<OntologyBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        int numberOfSections = OntologyBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        // check that all values are false
        for (boolean show : sections.values()) {
            assertTrue(!show);
        }
        vis.setVisibility(OntologyBean.Section.EXPRESSION, true);
        vis.setVisibility(OntologyBean.Section.PHENOTYPE, true);
        sections = vis.getSectionVisibilityMap();
        for (boolean show : sections.values()) {
            assertTrue(show);
        }

        vis.setSectionData(OntologyBean.Section.EXPRESSION, true);
        vis.setSectionData(OntologyBean.Section.PHENOTYPE, true);
        sections = vis.getSectionVisibilityMap();
        for (boolean show : sections.values()) {
            assertTrue(show);
        }
    }

    /**
     * Set default false visibilities all to true manually
     */
    @Test
    public void setVisibilityToTrueManuallyIsVisible() {
        SectionVisibility vis = new SectionVisibility<>(OntologyBean.Section.class, false);
        EnumMap<OntologyBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        List<String> visibleSections = vis.getVisibleSections();
        assertTrue(visibleSections != null);
        assertTrue(visibleSections.size() == 0);

        vis.setVisibility(OntologyBean.Section.EXPRESSION, true);
        visibleSections = vis.getVisibleSections();
        assertTrue(visibleSections.size() == 1);
        assertEquals(OntologyBean.Section.EXPRESSION.toString(), visibleSections.get(0));
    }

    /**
     * Set default false visibilities all to true vial showAll()
     */
    @Test
    public void setVisibilityToTrueViaShowAll() {
        SectionVisibility vis = new SectionVisibility<>(OntologyBean.Section.class, false);
        EnumMap<OntologyBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        int numberOfSections = OntologyBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        // check that all values are false
        for (boolean show : sections.values()) {
            assertTrue(!show);
        }
        vis.setAllSectionsVisible(true);
        sections = vis.getSectionVisibilityMap();
        for (boolean show : sections.values()) {
            assertTrue(show);
        }
        vis.setAllSectionsVisible(false);
        sections = vis.getSectionVisibilityMap();
        for (boolean show : sections.values()) {
            assertTrue(!show);
        }
    }

    /**
     * Test isVisible(Enum) method
     */
    @Test
    public void checkIsVisibleMethod() {
        SectionVisibility vis = new SectionVisibility<>(OntologyBean.Section.class);
        EnumMap<OntologyBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        int numberOfSections = OntologyBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        // check that all values are false
        assertTrue(!vis.isVisible(OntologyBean.Section.EXPRESSION));
        assertTrue(!vis.isVisible(OntologyBean.Section.PHENOTYPE));
        vis.setVisibility(OntologyBean.Section.EXPRESSION, true);
        vis.setVisibility(OntologyBean.Section.PHENOTYPE, true);
        vis.getSectionVisibilityMap();
        assertTrue(vis.isVisible(OntologyBean.Section.EXPRESSION));
        assertTrue(vis.isVisible(OntologyBean.Section.PHENOTYPE));

        assertTrue(!vis.hasData(OntologyBean.Section.EXPRESSION));
        assertTrue(!vis.hasData(OntologyBean.Section.PHENOTYPE));
        vis.setSectionData(OntologyBean.Section.EXPRESSION, true);
        vis.setSectionData(OntologyBean.Section.PHENOTYPE, true);
        vis.getSectionVisibilityMap();
        assertTrue(vis.hasData(OntologyBean.Section.EXPRESSION));
        assertTrue(vis.hasData(OntologyBean.Section.PHENOTYPE));
    }

    /**
     * Test setShowSection and setHideSection method
     */
    @Test
    public void checkSetHowSectionMethod() {
        SectionVisibility vis = new SectionVisibility<>(OntologyBean.Section.class);
        EnumMap<OntologyBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        int numberOfSections = OntologyBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        // check that all values are false
        assertTrue(!vis.isVisible(OntologyBean.Section.EXPRESSION));
        assertTrue(!vis.isVisible(OntologyBean.Section.PHENOTYPE));
        vis.setSectionVisible(OntologyBean.Section.EXPRESSION.toString());
        vis.setSectionVisible(OntologyBean.Section.PHENOTYPE.toString());
        vis.getSectionVisibilityMap();
        assertTrue(vis.isVisible(OntologyBean.Section.EXPRESSION));
        assertTrue(vis.isVisible(OntologyBean.Section.PHENOTYPE));

        vis.setSectionInvisible(OntologyBean.Section.EXPRESSION.toString());
        assertTrue(!vis.isVisible(OntologyBean.Section.EXPRESSION));
        assertTrue(vis.isVisible(OntologyBean.Section.PHENOTYPE));

    }

    /**
     * Test isVisible(string) method
     */
    @Test
    public void checkIsVisibleMethodString() {
        SectionVisibility vis = new SectionVisibility<>(OntologyBean.Section.class);
        EnumMap<OntologyBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        int numberOfSections = OntologyBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        // check that all values are false
        assertTrue(!vis.isVisible(OntologyBean.Section.EXPRESSION.toString()));
        assertTrue(!vis.isVisible(OntologyBean.Section.PHENOTYPE.toString()));

        sections = vis.getSectionDataExistence();
        assertTrue(sections != null);
        numberOfSections = OntologyBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        // check that all values are false
        assertTrue(!vis.hasData(OntologyBean.Section.EXPRESSION.toString()));
        assertTrue(!vis.hasData(OntologyBean.Section.PHENOTYPE.toString()));
    }

    @Test(expected = NullPointerException.class)
    public void testNoClassEnumerationClassProvidedExceptions() {
        new SectionVisibility<OntologyBean.Section>(null);
    }

    @Test(expected = NullPointerException.class)
    public void testSetVisibilityNoClassEnumerationClassProvidedExceptions() {
        SectionVisibility vis = new SectionVisibility<>(OntologyBean.Section.class);
        vis.setVisibility(null, true);
    }

    @Test
    public void hasData() {
        SectionVisibility vis = new SectionVisibility<>(OntologyBean.Section.class);
        EnumMap<OntologyBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        // no section data available
        assertTrue(!vis.isHasData());
        vis.setSectionData(OntologyBean.Section.EXPRESSION, true);
        // at least one seciont has data.
        assertTrue(vis.isHasData());
    }

    @Test
    public void getVisibleSectionsWithData() {
        SectionVisibility vis = new SectionVisibility<>(OntologyBean.Section.class);
        EnumMap<OntologyBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        String[] data = vis.getVisibleSectionsWithData();
        assertTrue(data != null);
        assertTrue(data.length == 0);

        vis.setSectionVisible(OntologyBean.Section.PHENOTYPE.toString());
        data = vis.getVisibleSectionsWithData();
        assertTrue(data.length == 0);
        vis.setSectionData(OntologyBean.Section.PHENOTYPE, true);
        data = vis.getVisibleSectionsWithData();
        assertTrue(data.length == 1);
        vis.setSectionVisible(OntologyBean.Section.EXPRESSION.toString());
        data = vis.getVisibleSectionsWithData();
        assertTrue(data.length == 1);
        vis.setSectionData(OntologyBean.Section.EXPRESSION, true);
        data = vis.getVisibleSectionsWithData();
        assertTrue(data.length == 2);
    }

    @Test
    public void getSectionsWithData() {
        SectionVisibility vis = new SectionVisibility<>(OntologyBean.Section.class);
        EnumMap<OntologyBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        String[] data = vis.getSectionsWithData();
        assertTrue(data != null);
        assertTrue(data.length == 0);

        vis.setSectionVisible(OntologyBean.Section.PHENOTYPE.toString());
        data = vis.getSectionsWithData();
        assertTrue(data.length == 0);
        vis.setSectionData(OntologyBean.Section.PHENOTYPE, true);
        data = vis.getSectionsWithData();
        assertTrue(data.length == 1);
        vis.setSectionData(OntologyBean.Section.EXPRESSION, true);
        data = vis.getSectionsWithData();
        assertTrue(data.length == 2);
        vis.setSectionVisible(OntologyBean.Section.EXPRESSION.toString());
        data = vis.getSectionsWithData();
        assertTrue(data.length == 2);
    }

    @Test
    public void atLeastOneSectionVisible() {
        SectionVisibility vis = new SectionVisibility<>(OntologyBean.Section.class);
        EnumMap<OntologyBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        assertTrue(!vis.isAnySectionVisible());
        vis.setSectionVisible(OntologyBean.Section.PHENOTYPE.toString());
        assertTrue(vis.isAnySectionVisible());
        vis.setShowSection(OntologyBean.Section.EXPRESSION.toString());
        assertTrue(vis.isAnySectionVisible());

    }


}