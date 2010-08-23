package org.zfin.mutant;

/**
 * This class defines the Zygosity.
 */
public class Zygosity {

    private String zdbID;
    private String name;
    private String abbreviation;
    private String definition;
    private String alleleDisplay;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getAlleleDisplay() {
        return alleleDisplay;
    }

    public void setAlleleDisplay(String alleleDisplay) {
        this.alleleDisplay = alleleDisplay;
    }

    public String getZygositySymbol() {
        if (name.equals("homozygous")) {
            return "-/-";
        } else if (name.equals("heterozygous")) {
            return "+/-";
        } else if (name.equals("hemizygous")) {
            return "+/0";
        } else if (name.equals("complex")) {
            return "c";
        } else if (name.equals("wild type")) {
            return "+/+";
        } else {
            return "";
        }
    }
}
