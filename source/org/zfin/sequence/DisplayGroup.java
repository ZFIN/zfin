package org.zfin.sequence;

import java.util.Set;

public class DisplayGroup implements Comparable<DisplayGroup>{


    private Long id;
    private GroupName groupName;
    private String definition;
    private Set<ReferenceDatabase> referenceDatabases;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GroupName getGroupName() {
        return groupName;
    }

    public void setGroupName(GroupName groupName) {
        this.groupName = groupName;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Set<ReferenceDatabase> getReferenceDatabases() {
        return referenceDatabases;
    }

    public void setReferenceDatabases(Set<ReferenceDatabase> referenceDatabases) {
        this.referenceDatabases = referenceDatabases;
    }

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
        TRANSCRIPT_EDIT_ADDABLE_MIRNA_NUCLEOTIDE_SEQUENCE("transcript edit addable miRNA nucleotide sequence")
        ;

        private String value;

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

        if(o instanceof DisplayGroup){
            DisplayGroup displayGroup = (DisplayGroup) o ;
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
