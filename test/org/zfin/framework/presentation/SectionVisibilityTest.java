package org.zfin.framework.presentation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import org.junit.Test;
import org.zfin.anatomy.presentation.AnatomySearchBean;

import java.util.EnumMap;
import java.util.List;

/**
 * Class used for showing and hiding sections on a detail page.
 * By default each section is set to hide.
 */
public class SectionVisibilityTest {

    @Test
    public void simpleSectionVisiblityDefaultFalse() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class);
        EnumMap<AnatomySearchBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        int numberOfSections = AnatomySearchBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        for (boolean show : sections.values()) {
            assertTrue(!show);
        }
        assertTrue(vis.isAllSectionsInvisible());

        sections = vis.getSectionDataExistence();
        assertTrue(sections != null);
        numberOfSections = AnatomySearchBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        for (boolean show : sections.values()) {
            assertTrue(!show);
        }
    }

    @Test
    public void simpleSectionVisiblityDefaultTrue() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class, true);
        EnumMap<AnatomySearchBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        int numberOfSections = AnatomySearchBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        for (boolean show : sections.values()) {
            assertTrue(show);
        }
        assertTrue(vis.isAllSectionsVisible());

        sections = vis.getSectionDataExistence();
        assertTrue(sections != null);
        numberOfSections = AnatomySearchBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        for (boolean show : sections.values()) {
            assertTrue(show);
        }
    }

    /**
     * Set default false visitilities all to true manually
     */
    @Test
    public void setVisbilityToTrueManually() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class, false);
        EnumMap<AnatomySearchBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        int numberOfSections = AnatomySearchBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        // check that all values are false
        for (boolean show : sections.values()) {
            assertTrue(!show);
        }
        vis.setVisibility(AnatomySearchBean.Section.ANATOMY_EXPRESSION, true);
        vis.setVisibility(AnatomySearchBean.Section.ANATOMY_PHENOTYPE, true);
        sections = vis.getSectionVisibilityMap();
        for (boolean show : sections.values()) {
            assertTrue(show);
        }

        vis.setSectionData(AnatomySearchBean.Section.ANATOMY_EXPRESSION, true);
        vis.setSectionData(AnatomySearchBean.Section.ANATOMY_PHENOTYPE, true);
        sections = vis.getSectionVisibilityMap();
        for (boolean show : sections.values()) {
            assertTrue(show);
        }
    }

    /**
     * Set default false visitilities all to true manually
     */
    @Test
    public void setVisbilityToTrueManuallyIsVisible() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class, false);
        EnumMap<AnatomySearchBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        List<String> visibleSections = vis.getVisibleSections();
        assertTrue(visibleSections != null);
        assertTrue(visibleSections.size() == 0);

        vis.setVisibility(AnatomySearchBean.Section.ANATOMY_EXPRESSION, true);
        visibleSections = vis.getVisibleSections();
        assertTrue(visibleSections.size() == 1);
        assertEquals(AnatomySearchBean.Section.ANATOMY_EXPRESSION.toString(), visibleSections.get(0));
    }

    /**
     * Set default false visitilities all to true vial showAll()
     */
    @Test
    public void setVisbilityToTrueViaShowAll() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class, false);
        EnumMap<AnatomySearchBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        int numberOfSections = AnatomySearchBean.Section.values().length;
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
    public void checkIsVisbileMethod() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class);
        EnumMap<AnatomySearchBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        int numberOfSections = AnatomySearchBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        // check that all values are false
        assertTrue(!vis.isVisible(AnatomySearchBean.Section.ANATOMY_EXPRESSION));
        assertTrue(!vis.isVisible(AnatomySearchBean.Section.ANATOMY_PHENOTYPE));
        vis.setVisibility(AnatomySearchBean.Section.ANATOMY_EXPRESSION, true);
        vis.setVisibility(AnatomySearchBean.Section.ANATOMY_PHENOTYPE, true);
        sections = vis.getSectionVisibilityMap();
        assertTrue(vis.isVisible(AnatomySearchBean.Section.ANATOMY_EXPRESSION));
        assertTrue(vis.isVisible(AnatomySearchBean.Section.ANATOMY_PHENOTYPE));

        assertTrue(!vis.hasData(AnatomySearchBean.Section.ANATOMY_EXPRESSION));
        assertTrue(!vis.hasData(AnatomySearchBean.Section.ANATOMY_PHENOTYPE));
        vis.setSectionData(AnatomySearchBean.Section.ANATOMY_EXPRESSION, true);
        vis.setSectionData(AnatomySearchBean.Section.ANATOMY_PHENOTYPE, true);
        sections = vis.getSectionVisibilityMap();
        assertTrue(vis.hasData(AnatomySearchBean.Section.ANATOMY_EXPRESSION));
        assertTrue(vis.hasData(AnatomySearchBean.Section.ANATOMY_PHENOTYPE));
    }

    /**
     * Test setShowSection and setHidesection method
     */
    @Test
    public void checkSetHowSectionMethod() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class);
        EnumMap<AnatomySearchBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        int numberOfSections = AnatomySearchBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        // check that all values are false
        assertTrue(!vis.isVisible(AnatomySearchBean.Section.ANATOMY_EXPRESSION));
        assertTrue(!vis.isVisible(AnatomySearchBean.Section.ANATOMY_PHENOTYPE));
        vis.setSectionVisible(AnatomySearchBean.Section.ANATOMY_EXPRESSION.toString());
        vis.setSectionVisible(AnatomySearchBean.Section.ANATOMY_PHENOTYPE.toString());
        sections = vis.getSectionVisibilityMap();
        assertTrue(vis.isVisible(AnatomySearchBean.Section.ANATOMY_EXPRESSION));
        assertTrue(vis.isVisible(AnatomySearchBean.Section.ANATOMY_PHENOTYPE));

        vis.setSectionInvisible(AnatomySearchBean.Section.ANATOMY_EXPRESSION.toString());
        sections = vis.getSectionVisibilityMap();
        assertTrue(!vis.isVisible(AnatomySearchBean.Section.ANATOMY_EXPRESSION));
        assertTrue(vis.isVisible(AnatomySearchBean.Section.ANATOMY_PHENOTYPE));

    }

    /**
     * Test isVisible(string) method
     */
    @Test
    public void checkIsVisbileMethodString() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class);
        EnumMap<AnatomySearchBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        int numberOfSections = AnatomySearchBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        // check that all values are false
        assertTrue(!vis.isVisible(AnatomySearchBean.Section.ANATOMY_EXPRESSION.toString()));
        assertTrue(!vis.isVisible(AnatomySearchBean.Section.ANATOMY_PHENOTYPE.toString()));

        sections = vis.getSectionDataExistence();
        assertTrue(sections != null);
        numberOfSections = AnatomySearchBean.Section.values().length;
        assertEquals(sections.size(), numberOfSections);
        // check that all values are false
        assertTrue(!vis.hasData(AnatomySearchBean.Section.ANATOMY_EXPRESSION.toString()));
        assertTrue(!vis.hasData(AnatomySearchBean.Section.ANATOMY_PHENOTYPE.toString()));
    }

    @Test
    public void testNoClassEnumerationClassProvidedExceptions() {
        try {
            SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(null);
        } catch (Exception e) {
            assertTrue(e.getMessage() != null);
            return;
        }
        fail("Should have thrown a NullPointerException");
    }

    @Test
    public void testSetVisibilityNoClassEnumerationClassProvidedExceptions() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class);
        try {
            vis.setVisibility(null, true);
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException);
            return;
        }
        fail("Should have thrown a NullPointerException");
    }

    @Test
    public void hasData() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class);
        EnumMap<AnatomySearchBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        // no section data available
        assertTrue(!vis.isHasData());
        vis.setSectionData(AnatomySearchBean.Section.ANATOMY_EXPRESSION, true);
        // at least one seciont has data.
        assertTrue(vis.isHasData());
    }

    @Test
    public void getVisibleSectionsWithData() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class);
        EnumMap<AnatomySearchBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        String[] data = vis.getVisibleSectionsWithData();
        assertTrue(data != null);
        assertTrue(data.length == 0);

        vis.setSectionVisible(AnatomySearchBean.Section.ANATOMY_PHENOTYPE.toString());
        data = vis.getVisibleSectionsWithData();
        assertTrue(data.length == 0);
        vis.setSectionData(AnatomySearchBean.Section.ANATOMY_PHENOTYPE, true);
        data = vis.getVisibleSectionsWithData();
        assertTrue(data.length == 1);
        vis.setSectionVisible(AnatomySearchBean.Section.ANATOMY_EXPRESSION.toString());
        data = vis.getVisibleSectionsWithData();
        assertTrue(data.length == 1);
        vis.setSectionData(AnatomySearchBean.Section.ANATOMY_EXPRESSION, true);
        data = vis.getVisibleSectionsWithData();
        assertTrue(data.length == 2);
    }

    @Test
    public void getSectionsWithData() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class);
        EnumMap<AnatomySearchBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        String[] data = vis.getSectionsWithData();
        assertTrue(data != null);
        assertTrue(data.length == 0);

        vis.setSectionVisible(AnatomySearchBean.Section.ANATOMY_PHENOTYPE.toString());
        data = vis.getSectionsWithData();
        assertTrue(data.length == 0);
        vis.setSectionData(AnatomySearchBean.Section.ANATOMY_PHENOTYPE, true);
        data = vis.getSectionsWithData();
        assertTrue(data.length == 1);
        vis.setSectionData(AnatomySearchBean.Section.ANATOMY_EXPRESSION, true);
        data = vis.getSectionsWithData();
        assertTrue(data.length == 2);
        vis.setSectionVisible(AnatomySearchBean.Section.ANATOMY_EXPRESSION.toString());
        data = vis.getSectionsWithData();
        assertTrue(data.length == 2);
    }

    @Test
    public void atLeastOneSectionVisible() {
        SectionVisibility vis = new SectionVisibility<AnatomySearchBean.Section>(AnatomySearchBean.Section.class);
        EnumMap<AnatomySearchBean.Section, Boolean> sections = vis.getSectionVisibilityMap();
        assertTrue(sections != null);
        assertTrue(!vis.isAnySectionVisible());
        vis.setSectionVisible(AnatomySearchBean.Section.ANATOMY_PHENOTYPE.toString());
        assertTrue(vis.isAnySectionVisible());
        vis.setShowSection(AnatomySearchBean.Section.ANATOMY_EXPRESSION.toString());
        assertTrue(vis.isAnySectionVisible());

    }


}