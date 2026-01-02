package org.zfin.sequence;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "foreign_db_contains_display_group")
public class DisplayGroup implements Comparable<DisplayGroup>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fdbcdg_pk_id", nullable = false)
    private Long id;

    @Column(name = "fdbcdg_name", nullable = false)
    @org.hibernate.annotations.Type(value = org.zfin.framework.StringEnumValueUserType.class, parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.sequence.DisplayGroup$GroupName")})
    @JsonView(View.SequenceDetailAPI.class)
    private GroupName groupName;

    @Column(name = "fdbcdg_definition", nullable = false)
    private String definition;

    @OneToMany(mappedBy = "displayGroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<DisplayGroupMember> displayGroupMembers;

    public Set<ReferenceDatabase> getReferenceDatabases() {
        return this.displayGroupMembers.stream()
                .map(DisplayGroupMember::getReferenceDatabase)
                .collect(java.util.stream.Collectors.toSet());
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public enum GroupName {
        ADDABLE_NUCLEOTIDE_SEQUENCE("addable nucleotide sequence"),
        DBLINK_ADDING_ON_CLONE_EDIT("dblink adding on clone-edit"),
        DBLINK_ADDING_ON_MARKER_EDIT("dblink adding on marker-edit"),
        DBLINK_ADDING_ON_TRANSCRIPT_EDIT("dblink adding on transcript-edit"),
        DISPLAYED_NUCLEOTIDE_SEQUENCE("displayed nucleotide sequence"),
        DISPLAYED_PROTEIN_SEQUENCE("displayed protein sequence"),
        OTHER_MARKER_PAGES("other marker pages"),
        MARKER_LINKED_SEQUENCE("marker linked sequence"),
        TRANSCRIPT_LINKED_SEQUENCE("transcript linked sequence"),
        GENE_EDIT_ADDABLE_PROTEIN_SEQUENCE("gene edit addable protein sequence"),
        GENE_EDIT_ADDABLE_NUCLEOTIDE_SEQUENCE("gene edit addable nucleotide sequence"),
        MICROARRAY_EXPRESSION("microarray expression"),
        MICRORNA_TARGETS("microrna targets"),
        SUMMARY_PAGE("summary page"),
        GENE_VIEW_STEM_LOOP("gene view stem loop"),
        TRANSCRIPT_EDIT_ADDABLE_PROTEIN_SEQUENCE("transcript edit addable protein sequence"),
        TRANSCRIPT_EDIT_ADDABLE_NUCLEOTIDE_SEQUENCE("transcript edit addable nucleotide sequence"),
        TRANSCRIPT_EDIT_ADDABLE_MIRNA_NUCLEOTIDE_SEQUENCE("transcript edit addable miRNA nucleotide sequence"),
        HIDDEN_DBLINKS("hidden dblinks"),
        PLASMIDS("plasmids"),
        PATHWAYS("pathways")
        ;

        private final String value;

        @JsonValue
        public String getValue() {
            return value;
        }

        GroupName(String value) {
            this.value = value;
        }

        public String toString(){
            return this.value ;
        }

        public static GroupName getGroup(String group) {
            for (GroupName groupName : values()) {
                if (groupName.toString().equals(group))
                    return groupName;
            }
            throw new RuntimeException("No reference displaygroup of string " + group + " found.");
        }
    }

    public boolean equals(Object o){

        if(o instanceof DisplayGroup displayGroup){
            if(id!=null && displayGroup.getId()!=null){
                return (id==displayGroup.getId() || id.equals(displayGroup.getId()) ) ;
            }
            else
            if(groupName!=null && displayGroup.getGroupName()!=null){
                return groupName==displayGroup.getGroupName() ;
            }
        }
        return false ;
    }

    public int compareTo(DisplayGroup o) {
        return groupName.compareTo(o.getGroupName()) ;
    }
}
