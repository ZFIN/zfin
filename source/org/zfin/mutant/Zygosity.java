package org.zfin.mutant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Getter
@Setter
@Entity
@Table(name = "zygocity")
public class Zygosity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Zygosity")
    @GenericGenerator(name = "Zygosity",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @Parameter(name = "type", value = "ZYG")
            })
    @Column(name = "zyg_zdb_id")
    private String zdbID;
    @Column(name = "zyg_name")
    private String name;
    @Column(name = "zyg_abbrev")
    private String abbreviation;
    @Column(name = "zyg_definition")
    private String definition;
    @Column(name = "zyg_allele_display")
    private String alleleDisplay;
    @Column(name = "zyg_geno_ont_id")
    private String genoOntologyID;

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

    public enum Type {
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
