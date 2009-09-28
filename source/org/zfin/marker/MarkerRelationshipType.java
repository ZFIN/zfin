package org.zfin.marker;

/**
 * mapped to marker_relationship_type
 */
public class MarkerRelationshipType {
    private String name;
    private MarkerTypeGroup firstMarkerTypeGroup;
    private MarkerTypeGroup secondMarkerTypeGroup;
    private String firstToSecondLabel;
    private String secondToFirstLabel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MarkerTypeGroup getFirstMarkerTypeGroup() {
        return firstMarkerTypeGroup;
    }

    public void setFirstMarkerTypeGroup(MarkerTypeGroup firstMarkerTypeGroup) {
        this.firstMarkerTypeGroup = firstMarkerTypeGroup;
    }

    public MarkerTypeGroup getSecondMarkerTypeGroup() {
        return secondMarkerTypeGroup;
    }

    public void setSecondMarkerTypeGroup(MarkerTypeGroup secondMarkerTypeGroup) {
        this.secondMarkerTypeGroup = secondMarkerTypeGroup;
    }

    public String getFirstToSecondLabel() {
        return firstToSecondLabel;
    }

    public void setFirstToSecondLabel(String firstToSecondLabel) {
        this.firstToSecondLabel = firstToSecondLabel;
    }

    public String getSecondToFirstLabel() {
        return secondToFirstLabel;
    }

    public void setSecondToFirstLabel(String secondToFirstLabel) {
        this.secondToFirstLabel = secondToFirstLabel;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarkerRelationshipType that = (MarkerRelationshipType) o;

        return false==(name != null ? false==name.equals(that.name) : that.name != null);

        }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (firstMarkerTypeGroup != null ? firstMarkerTypeGroup.hashCode() : 0);
        result = 31 * result + (secondMarkerTypeGroup != null ? secondMarkerTypeGroup.hashCode() : 0);
        result = 31 * result + (firstToSecondLabel != null ? firstToSecondLabel.hashCode() : 0);
        result = 31 * result + (secondToFirstLabel != null ? secondToFirstLabel.hashCode() : 0);
        return result;
    }
}
