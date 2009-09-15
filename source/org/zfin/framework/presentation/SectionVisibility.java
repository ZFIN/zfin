package org.zfin.framework.presentation;

import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;

/**
 * This class can be used to store the visbility and data existence of a set of sections.
 * <p/>
 * Visbility: (Expand/Collapse)
 * A visible section can mean that a section is expanded while a non-visible section could mean to collapse
 * the section info. It faciliates the logic to create a hyperlink 'show' / 'hide'.
 * <p/>
 * Data Existence:
 * Expansion and collapse of section data is defined by the visibility attribute while the data existence
 * attribute can be used to decide if there is data available for a given section and the show-link should be
 * displayed at all.
 * <p/>
 * The constructor takes an enumeration class in the constructor, that loops over all enumeration elements,
 * one per section.
 */
public class SectionVisibility<K extends Enum<K>> {

    /**
     * For each enumeration element the boolean flags if the section is visible or not,
     * i.e. is being hidden or displayed. Each enumeration corresponds to a section.
     */
    private EnumMap<K, Boolean> sectionDisplayVisibility;
    /**
     * For each enumeration element the boolean flags if the section has any data associated
     * with it. This can be used to decide if the show/hide links should be displayed at all.
     */
    private EnumMap<K, Boolean> sectionDataExistence;

    /**
     * Create object with all sections and data set to false;
     *
     * @param enumType Enumeration Class name, e.g. AnatomySearchBean.Section.class
     */
    public SectionVisibility(Class<K> enumType) {
        this(enumType, false);
    }

    /**
     * Create object with all sections set to the value given (show/hide) but data set to false
     *
     * @param enumType Enumeration Class name, e.g. AnatomySearchBean.Section.class
     * @param show     boolean
     */
    public SectionVisibility(Class<K> enumType, boolean show) {
        super();
        if (enumType == null)
            throw new NullPointerException("No class provided");
        sectionDisplayVisibility = new EnumMap<K, Boolean>(enumType);
        sectionDataExistence = new EnumMap<K, Boolean>(enumType);
        K[] enums = enumType.getEnumConstants();
        for (K enumeration : enums) {
            sectionDisplayVisibility.put(enumeration, show);
        }
        for (K enumeration : enums) {
            sectionDataExistence.put(enumeration, show);
        }
    }

    /**
     * Retrieve the enumeration map used internally.
     * For each section there is a boolean indicating its visibility.
     *
     * @return enumeration map.
     */
    public EnumMap<K, Boolean> getSectionVisibilityMap() {
        return sectionDisplayVisibility;
    }

    /**
     * Retrieve the section data enumeration map.
     * For each section there is a boolean indicating existence of non-existence of data.
     *
     * @return enumeration map
     */
    public EnumMap<K, Boolean> getSectionDataExistence() {
        return sectionDataExistence;
    }

    /**
     * Set visibility for a given enumeration element to the boolean provided.
     *
     * @param enumeration enumeration element e.g. AnatomySearchBean.Section
     * @param show        boolean show/hide
     */
    public void setVisibility(K enumeration, boolean show) {
        Boolean section = sectionDisplayVisibility.get(enumeration);
        if (section == null)
            throw new NullPointerException("No enumeration " + enumeration.toString() + " found!");

        sectionDisplayVisibility.put(enumeration, show);
    }

    /**
     * Set data availability for a given section to true/false
     *
     * @param enumeration enumeration element e.g. AnatomySearchBean.Section
     * @param exists      boolean exist/doesn't exists
     */
    public void setSectionData(K enumeration, boolean exists) {
        Boolean section = sectionDataExistence.get(enumeration);
        if (section == null)
            throw new NullPointerException("No enumeration " + enumeration.toString() + " found!");

        sectionDataExistence.put(enumeration, exists);
    }

    /**
     * Check visibility for a given section (enumeration element).
     *
     * @param enumeration enumeration element, e.g. AnatomySearchBean.Section
     * @return boolean show/hide
     */
    public boolean isVisible(K enumeration) {
        Boolean section = sectionDisplayVisibility.get(enumeration);
        if (section == null)
            throw new NullPointerException("No enumeration " + enumeration.toString() + " found!");

        return sectionDisplayVisibility.get(enumeration);
    }

    /**
     * Check if data exist for a given section.
     *
     * @param enumeration enumeration element, e.g. AnatomySearchBean.Section
     * @return boolean exists/doesn't exists
     */
    public boolean hasData(K enumeration) {
        Boolean section = sectionDataExistence.get(enumeration);
        if (section == null)
            throw new NullPointerException("No enumeration " + enumeration.toString() + " found!");

        return sectionDataExistence.get(enumeration);
    }

    public boolean isVisible(String sectionName) {
        K enumeration = getSectionEnumeration(sectionName);

        Boolean section = sectionDisplayVisibility.get(enumeration);
        if (section == null)
            throw new NullPointerException("No enumeration " + enumeration.toString() + " found!");

        return sectionDisplayVisibility.get(enumeration);
    }

    /**
     * Check if for a given section name data exist.
     *
     * @param sectionName name of the section
     * @return boolean exists/doesn't exists
     */
    public boolean hasData(String sectionName) {
        K enumeration = null;
        for (K enumerationIntern : sectionDataExistence.keySet()) {
            if (enumerationIntern.toString().equals(sectionName))
                enumeration = enumerationIntern;
        }
        if (enumeration == null)
            throw new RuntimeException("No enumeration element with name " + sectionName + " found");

        Boolean section = sectionDataExistence.get(enumeration);
        if (section == null)
            throw new NullPointerException("No enumeration " + enumeration.toString() + " found!");

        return sectionDataExistence.get(enumeration);
    }

    /**
     * Returns true if any section has data associated, otherwise false.
     *
     * @return boolean
     */
    public boolean isHasData() {
        for (K enumeration : sectionDataExistence.keySet()) {
            if (sectionDataExistence.get(enumeration))
                return true;
        }
        return false;
    }

    /**
     * Returns true if all sections are visible, otherwise false;
     *
     * @return boolean
     */
    public boolean isAllSectionsVisible() {
        for (K enumerationIntern : sectionDisplayVisibility.keySet()) {
            if (!isVisible(enumerationIntern))
                return false;
        }
        return true;
    }

    /**
     * Returns true if all sections are hidden, otherwise false;
     *
     * @return boolean
     */
    public boolean isAllSectionsInvisible() {
        for (K enumerationIntern : sectionDisplayVisibility.keySet()) {
            if (isVisible(enumerationIntern))
                return false;
        }
        return true;
    }

    /**
     * Checks is at least one section is visible.
     *
     * @return boolean at least one section is visible.
     */
    public boolean isAnySectionVisible() {
        return getVisibleSections().size() > 0;
    }

    /**
     * Sets all sections to be visible or invisible.
     *
     * @param show boolean
     */
    public void setAllSectionsVisible(boolean show) {
        if (show) {
            for (K enumerationIntern : sectionDisplayVisibility.keySet()) {
                setVisibility(enumerationIntern, true);
            }
        } else {
            for (K enumerationIntern : sectionDisplayVisibility.keySet()) {
                setVisibility(enumerationIntern, false);
            }
        }
    }

    /**
     * Sets a given section to be visible.
     * Use this method if you have a comma-delimited list of section names.
     *
     * @param sectionNames string
     */
    public void setSectionVisible(String sectionNames) {
        if (sectionNames == null)
            throw new RuntimeException("No section name found");
        String[] sectionNameArray = sectionNames.split(",");
        setSectionsVisible(sectionNameArray);
    }

    /**
     * Sets all given sections to be visible.
     *
     * @param sectionNames array of sections.
     */
    public void setSectionsVisible(String[] sectionNames) {
        for (String sectionName : sectionNames) {
            K enumeration = getSectionEnumeration(sectionName);
            setVisibility(enumeration, true);
        }
    }

    /**
     * Hide a given section.
     *
     * @param sectionName string
     */
    public void setSectionInvisible(String sectionName) {
        K enumeration = getSectionEnumeration(sectionName);
        setVisibility(enumeration, false);
    }

    /**
     * Setter used for autopoulation of a form bean.
     * Sets the visibility of a section.
     *
     * @param sectionNames string comma-delimited list of sections
     */
    public void setShowSection(String sectionNames) {
        String[] sectionNameArray = sectionNames.split(",");
        for (String sectionName : sectionNameArray) {
            K enumeration = getSectionEnumeration(sectionName);
            setVisibility(enumeration, true);
        }
    }

    /**
     * Retrieve all sections that are visible.
     *
     * @return list
     */
    public List<String> getVisibleSections() {
        List<String> visibleSection = new ArrayList<String>();
        for (K enumeration : sectionDisplayVisibility.keySet()) {
            if (isVisible(enumeration))
                visibleSection.add(enumeration.toString());
        }
        return visibleSection;
    }

    /**
     * Retrieve all sections that are visible and have data associated with.
     *
     * @return list
     */
    public String[] getVisibleSectionsWithData() {
        List<String> visibleSection = new ArrayList<String>();
        for (K enumeration : sectionDisplayVisibility.keySet()) {
            if (isVisible(enumeration) && hasData(enumeration))
                visibleSection.add(enumeration.toString());
        }
        return visibleSection.toArray(new String[visibleSection.size()]);
    }

    /**
     * Retrieve all sections that have data associated with irrespective of
     * their visibility.
     *
     * @return list
     */
    public String[] getSectionsWithData() {
        List<String> sectionsWithData = new ArrayList<String>();
        for (K enumeration : sectionDisplayVisibility.keySet()) {
            if (hasData(enumeration))
                sectionsWithData.add(enumeration.toString());
        }
        return sectionsWithData.toArray(new String[sectionsWithData.size()]);
    }

    private K getSectionEnumeration(String sectionName) {
        K enumeration = null;
        for (K enumerationIntern : sectionDisplayVisibility.keySet()) {
            if (enumerationIntern.toString().equals(sectionName))
                enumeration = enumerationIntern;
        }
        if (enumeration == null)
            throw new RuntimeException("No enumeration element with name " + sectionName + " found");
        return enumeration;
    }

    public static enum Action {
        SHOW_SECTION("showSection"),
        HIDE_SECTION("hideSection"),
        SHOW_ALL("showAll"),
        HIDE_ALL("hideAll");
        private String value;

        private Action(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }

        public static Action[] getActionItems() {
            return values();
        }
    }
}
