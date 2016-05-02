package org.zfin.infrastructure;

import org.hibernate.annotations.DiscriminatorFormula;

import javax.persistence.*;

/**
 * This class maps to a fast search table in the db for lookup of
 * marker names and aliases and other entities.
 */
@Entity
@Table(name = "all_map_names")
@DiscriminatorFormula("CASE get_obj_type(allmapnm_zdb_id)" +
        "                                    WHEN 'ALT'  THEN     'Alt---'" +
        "                                    WHEN 'GENO' THEN     'Geno--'" +
        "                                    WHEN 'MRPHLNO' THEN  'Morpho'" +
        "                                    ELSE                 'Marker'" +
        "                                 END")
public abstract class AllNamesFastSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allmapnm_serial_id")
    private int id;
    @Column(name = "allmapnm_name")
    private String name;
    @Column(name = "allmapnm_name_lower")
    private String nameLowerCase;
    @Column(name = "allmapnm_precedence")
    private String precedence;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameLowerCase() {
        return nameLowerCase;
    }

    public void setNameLowerCase(String nameLowerCase) {
        this.nameLowerCase = nameLowerCase;
    }

    public String getPrecedence() {
        return precedence;
    }

    public void setPrecedence(String precedence) {
        this.precedence = precedence;
    }

    public static Precedence[] getGenePrecedences() {
        return new Precedence[]{Precedence.GENE_ALIAS, Precedence.GENE_SYMBOL, Precedence.CURRENT_NAME, Precedence.CURRENT_SYMBOL, Precedence.PREVIOUS_NAME};
    }

    public enum Precedence {
        ACCESSION_NUMBER("Accession number"),
        CURRENT_NAME("Current name"),
        CURRENT_SYMBOL("Current symbol"),
        GENE_ALIAS("Gene alias"),
        GENE_NAME("Gene name"),
        GENE_SYMBOL("Gene symbol"),
        PREVIOUS_NAME("Previous name");

        private String name;

        Precedence(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }
}