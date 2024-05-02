package org.zfin;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

import jakarta.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@Entity
@Table(name = "organism")
public class Species implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organism_taxid")
    @JsonView(View.API.class)
    private int taxonomyID;
    @Column(name = "organism_species")
    @JsonView(View.API.class)
    private String speciesName;
    @Column(name = "organism_common_name")
    @JsonView(View.API.class)
    private String commonName;
    @Column(name = "organism_display_order")
    int displayOrder;
    @Column(name = "organism_is_ab_immun")
    boolean antibodyImmunogen;
    @Column(name = "organism_is_ab_host")
    boolean antibodyHost;
    @Transient
    private Type organism;

    public enum Type {

        ZEBRAFISH("Zebrafish", 1),
        HUMAN("Human", 2),
        MOUSE("Mouse", 3),
        FRUIT_FLY("Fruit fly", 4),
        YEAST("Yeast", 5);

        private String value;
        private int index;

        Type(String value, int index) {
            this.value = value;
            this.index = index;
        }

        public String toString() {
            return this.value;
        }

        public int getIndex() {
            return this.index;
        }

        static public Type getType(String value) {
            for (Type item : values()) {
                if (item.toString().equals(value))
                    return item;
            }
            return null;
        }

    }

}
