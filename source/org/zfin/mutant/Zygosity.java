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
    private String genoOntologyID;

    public String getGenoOntologyID() {
        return genoOntologyID;
    }

    public void setGenoOntologyID(String genoOntologyID) {
        this.genoOntologyID = genoOntologyID;
    }


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

    public Type getType() {
        return Type.getZygosity(name);
    }

    public String getMutantZygosityDisplay(String featureName) {
        StringBuilder builder = new StringBuilder(featureName);
        if (Type.getZygosity(name).equals(Type.HOMOZYGOUS)) {
            builder.append("/");
            builder.append(featureName);
        } else if (Type.getZygosity(name).equals(Type.HETEROZYGOUS)) {
            builder.append("/");
            builder.append("+");
        }
        return builder.toString();
    }

    enum Type {
        HOMOZYGOUS("homozygous", "2"),
        HETEROZYGOUS("heterozygous", "1"),
        UNKNOWN("unknown", "U"),
        WILDTYPE("wild type", "W"),
        COMPLEX("complex", "C");
        private String name;
        private String symbol;

        Type(String name, String symbol) {
            this.name = name;
            this.symbol = symbol;
        }

        public String getName() {
            return name;
        }

        public String getSymbol() {
            return symbol;
        }

        public static Type getZygosity(String name) {
            for (Type type : values())
                if (type.getName().equals(name))
                    return type;
            return null;
        }
    }

}
