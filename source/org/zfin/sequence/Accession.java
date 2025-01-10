/**
 *  Class Accession.
 */
package org.zfin.sequence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.zfin.Species;
import org.zfin.marker.Marker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.zfin.util.ZfinPropertyUtils.getPropertyOrNull;

/**
 * A wrapper around the accession_bank table.
 */
@Setter
@Getter
@Entity
@Table(name = "accession_bank")
public class Accession implements Comparable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accbk_pk_id", nullable = false)
    private Long ID;

    @Column(name = "accbk_acc_num")
    private String number;

    @Column(name = "accbk_defline")
    private String defline;

    @Column(name = "accbk_length")
    private Integer length;

    @Column(name = "accbk_abbreviation")
    private String abbreviation;

    @Transient
    private Set<LinkageGroup> linkageGroups;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accbk_fdbcont_zdb_id", nullable = false)
    private ReferenceDatabase referenceDatabase;

    @OneToMany(mappedBy = "accession", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<EntrezProtRelation> relatedEntrezAccessions;
//    private Set<Accession> relatedAccessions;

//    @OneToMany(fetch = FetchType.LAZY)
//    @JoinFormula("(select dblink_acc_num from db_link where dblink_acc_num = accbk_acc_num)")
    @OneToMany(mappedBy = "accession", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<DBLink> dbLinks;

    public String getURL() {
        return referenceDatabase.getBaseURL();
    }

    //    todo: update to use get/setMarkerDBLinks
    public List<Marker> getMarkers() {
        return getMarkerDBLinks().stream().map(MarkerDBLink::getMarker).collect(Collectors.toList());
    }

    public Set<MarkerDBLink> getMarkerDBLinks() {
        if (getDbLinks() == null) {
            return Set.of();
        }
        return getDbLinks().stream()
                .filter(link -> link instanceof MarkerDBLink)
                .map(link -> (MarkerDBLink) link)
                .collect(Collectors.toSet());
    }

    public List<Marker> getBlastableMarkers() {
        List<Marker> markers = new ArrayList<Marker>();
        for (MarkerDBLink link : getBlastableMarkerDBLinks()) {
            if (link.getMarker() != null) {
                markers.add(link.getMarker());
            }
        }
        return markers;
    }

    /**
     * Get all the blastable marker db links.
     * This is a subset of DBLinks that are of type MarkerDBLink and have a reference database that is of type sequence and either RNA or Polypeptide.
     * @return
     */
    public Set<MarkerDBLink> getBlastableMarkerDBLinks() {
        return getMarkerDBLinks().stream()
                // filter out the ones that are not of super type sequence
                .filter(l -> ForeignDBDataType.SuperType.SEQUENCE.equals(
                        getPropertyOrNull(l, "referenceDatabase.foreignDBDataType.superType")))
                // filter out the ones that are not of type RNA or Polypeptide
                .filter(l -> Set.of(
                                ForeignDBDataType.DataType.RNA,
                                ForeignDBDataType.DataType.POLYPEPTIDE)
                        .contains(getPropertyOrNull(
                                l, "referenceDatabase.foreignDBDataType.dataType")))
                .collect(Collectors.toSet());
    }

    public int compareTo(Object o) {
        if (o instanceof Accession a) {
            return a.getNumber().compareTo(getNumber());
        } else {
            return 0;
        }
    }

    public Species.Type getOrganism() {
        if (CollectionUtils.isEmpty(relatedEntrezAccessions)) {
            if (!this.getReferenceDatabase().getOrganism().isEmpty()){
                    if (referenceDatabase.getOrganism().toString().equalsIgnoreCase("mouse")){
                            return Species.Type.MOUSE;
                    }
                    else if (referenceDatabase.getOrganism().toString().equalsIgnoreCase("human")){
                        return Species.Type.HUMAN;
                    }
                    else if (referenceDatabase.getOrganism().toString().equalsIgnoreCase("zebrafish")){
                        return Species.Type.ZEBRAFISH;
                    }
            }
            else {
                return Species.Type.ZEBRAFISH;
            }
        }
        Species.Type species = null;
        for (EntrezProtRelation entrezProtRelation : relatedEntrezAccessions) {
            Species.Type entrezSpecies = entrezProtRelation.getOrganism();
            if (species == null) {
                species = entrezSpecies;
            } else {
                if (species != entrezSpecies) {
                    throw new RuntimeException("Same accessions for different species, not allowed!!");
                }
            }

        }
        return species;
    }

    public boolean equals(Object o) {
        if (o instanceof Accession a) {
            if (a.getNumber().equals(getNumber())
                    &&
                    a.getReferenceDatabase().getForeignDB().getDbName().equals(getReferenceDatabase().getForeignDB().getDbName())
                    ) {
                return true;
            }
        }
        return false;
    }
}
