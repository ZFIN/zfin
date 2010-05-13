package org.zfin.gbrowse;

/**
 * A direct mapping of the Bio::Seqfeature::Store schema contig table
 * <p/>
 * For zfin,these are chromosomes.
 */
public class GBrowseContig implements Comparable {

    public static final String AB = "AB";
    public static final String U = "U";

    public static final String AB_NOTE = "Chromosome AB groups all the sequenced clones for a PAC library made from the AB strain.";
    public static final String U_NOTE = "Chromosome U contains finished clones which have not been placed in the physical map. ";


    public Integer id;
    public String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int compareTo(Object o) {
        //todo: this seems like it should do some zeropadding... do we have that built in?
        GBrowseContig other = (GBrowseContig) o;
        if (other == null) return 1;
        return getName().compareTo(other.getName());
    }

    public String toString() {
        return getName();
    }
}
