package org.zfin.sequence.gff;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.framework.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "gff3_ncbi_attribute")
public class Gff3NcbiAttributePair extends BaseEntity implements Comparable<Gff3NcbiAttributePair> {

    @Id
    @GeneratedValue(generator = "sequence-generator")
    @GenericGenerator(
        name = "sequence-generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @org.hibernate.annotations.Parameter(name = "sequence_name", value = "gff3_ncbi_attribute_seq"),
            @org.hibernate.annotations.Parameter(name = "initial_value", value = "100"),
            @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
        }
    )
    @Column(name = "gna_pk_id", nullable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name = "gna_gff_pk_id")
    private Gff3Ncbi gff3Ncbi;

    @Column(name = "gna_key")
    private String key;

    @Column(name = "gna_value")
    private String value;

    public String getGeneID() {
        if (!key.equals("Dbxref")) {
            return null;
        }
        String[] IDKeyValuePairs = getValue().split(",");
        for (String IDKeyValuePair : IDKeyValuePairs) {
            String[] iDKeyValuePair = IDKeyValuePair.split(":");
            if (iDKeyValuePair.length == 2 && iDKeyValuePair[0].equals("GeneID")) {
                return iDKeyValuePair[1];
            }
        }
        return null;
    }

    private static List<String> sortedAttributeKeys = new ArrayList<>(List.of("ID", "bxref", "gene_id"));

    @Override
    public int compareTo(Gff3NcbiAttributePair pair) {
        int thisIndex = sortedAttributeKeys.indexOf(this.getKey());
        int thatIndex = sortedAttributeKeys.indexOf(pair.getKey());
        if (thisIndex == -1 && thatIndex == -1) {
            return this.getKey().compareTo(pair.getKey());
        } else if (thisIndex == -1) {
            return 1;
        } else if (thatIndex == -1) {
            return -1;
        } else {
            return Integer.compare(thisIndex, thatIndex);
        }
    }
}


