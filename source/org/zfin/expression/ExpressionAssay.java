package org.zfin.expression;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.zfin.framework.api.View;

/**
 * Domain object
 */
@Entity
@Table(name = "expression_pattern_assay")
@Immutable
@Getter
@Setter
public class ExpressionAssay implements Comparable<ExpressionAssay> {

    @Id
    @Column(name = "xpatassay_name")
    @JsonView({View.API.class, View.UI.class})
    private String name;

    @Column(name = "xpatassay_comments")
    private String comments;

    @Column(name = "xpatassay_display_order")
    private int displayOrder;

    @Column(name = "xpatassay_abbrev")
    @JsonView({View.API.class, View.UI.class})
    private String abbreviation;

    @Transient
    private boolean immunogen;

    @Override
    public int compareTo(ExpressionAssay anotherAssay) {
        if (anotherAssay == null)
            return 1;
        return displayOrder - anotherAssay.getDisplayOrder();
    }

    public static boolean isAntibodyAssay(String assayName) {
        Type assay = Type.getAssay(assayName);
        return assay == Type.WESTERN_BLOT || assay == Type.IMMUNOHISTOCHEMISTRY || assay == Type.OTHER;
    }

    public enum Type {

        WESTERN_BLOT("Western blot"),
        IMMUNOHISTOCHEMISTRY("Immunohistochemistry"),
        OTHER("other"),
        CDNA_CLONES("cDNA clones");

        private String name;

        private Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Type getAssay(String assayName) {
            for (Type t : values()) {
                if (t.getName().equals(assayName))
                    return t;
            }
            throw new RuntimeException("No Assay name " + assayName + " found.");
        }

    }
}
