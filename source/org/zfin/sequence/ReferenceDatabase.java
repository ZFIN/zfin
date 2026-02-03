/**
 *  Class ReferenceDatabase.
 */
package org.zfin.sequence;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.zfin.framework.api.View;
import org.zfin.sequence.blast.Database;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
@Entity
@Table(name = "foreign_db_contains")
public class ReferenceDatabase implements Comparable<ReferenceDatabase>, Serializable {

    @Id
    @GeneratedValue(generator = "zdbIdGeneratorForReferenceDatabase")
    @GenericGenerator(name = "zdbIdGeneratorForReferenceDatabase", strategy = "org.zfin.database.ZdbIdGenerator", parameters = {
            @Parameter(name = "type", value = "FDBCONT"),
            @Parameter(name = "insertActiveData", value = "true")
    })
    @Column(name = "fdbcont_zdb_id", nullable = false)
    @JsonView(View.SequenceDetailAPI.class)
    private String zdbID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fdbcont_fdb_db_id", nullable = false)
    @JsonView({View.MarkerRelationshipAPI.class, View.SequenceDetailAPI.class})
    private ForeignDB foreignDB;

    @Column(name = "fdbcont_organism_common_name", nullable = false)
    private String organism;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fdbcont_fdbdt_id", nullable = false)
    @JsonView(View.SequenceDetailAPI.class)
    private ForeignDBDataType foreignDBDataType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fdbcont_primary_blastdb_zdb_id", nullable = false)
    private Database primaryBlastDatabase;

    @OneToMany(mappedBy = "referenceDatabase", fetch = FetchType.LAZY)
    @JsonView(View.SequenceDetailAPI.class)
    private Set<DisplayGroupMember> displayGroupMembers;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "int_fdbcont_analysis_tool",
            joinColumns = @JoinColumn(name = "ifat_fdbcont_zdb_id"),
            inverseJoinColumns = @JoinColumn(name = "ifat_blastdb_zdb_id")
    )
    private List<Database> relatedBlastDbs;

    @OneToMany(mappedBy = "referenceDatabase", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ReferenceDatabaseValidationRule> validationRules;

    public String getBaseURL() {
        return foreignDB.getDbUrlPrefix();
    }

    public void setBaseURL(String baseURL) {
        foreignDB.setDbUrlPrefix(baseURL);
    }

    public String toString() {
        String returnString = "";
        returnString += zdbID + " ";
        returnString += organism + " ";
        if (foreignDB != null) {
            returnString += foreignDB.getDbName() + " ";
        }
        if (primaryBlastDatabase != null) {
            returnString += primaryBlastDatabase.toString();
        }
        return returnString;
    }

    public int compareTo(ReferenceDatabase otherRefDB) {
        if (otherRefDB == null) return +1;

        if (foreignDB.compareTo(otherRefDB.getForeignDB()) != 0)
            return foreignDB.compareTo(otherRefDB.getForeignDB());

        return foreignDB.getDbName().compareTo(otherRefDB.getForeignDB().getDbName());
    }

    public boolean isInDisplayGroup(DisplayGroup.GroupName groupName) {
        for (DisplayGroup displayGroup : getDisplayGroups()) {
            if (displayGroup.getGroupName() == groupName) {
                return true;
            }
        }
        return false;
    }

    public Set<DisplayGroup> getDisplayGroups() {
        return getDisplayGroupMembers().stream()
                .map(DisplayGroupMember::getDisplayGroup)
                .collect(Collectors.toSet());
    }

    public String getView() {
        return foreignDB.getDbName().toString() + " " + foreignDBDataType.getDataType() + " " + foreignDBDataType.getSuperType();
    }

    public List<Database> getOrderedRelatedBlastDB() {
        List<Database> sortedDatabases = new ArrayList<>();
        sortedDatabases.addAll(relatedBlastDbs);
        Collections.sort(sortedDatabases, (o1, o2) -> {
            if (o1.getToolDisplayOrder() != null && o2.getToolDisplayOrder() != null) {
                return o1.getToolDisplayOrder().compareTo(o2.getToolDisplayOrder());
            } else if (o1.getToolDisplayOrder() == null && o2.getToolDisplayOrder() == null) {
                return o1.getName().compareTo(o2.getName());
            } else if (o1.getToolDisplayOrder() == null && o2.getToolDisplayOrder() != null) {
                return 1;
            } else {
                return -1;
            }
        });
        return sortedDatabases;
    }

    public boolean isShortSequence() {
        String abbrevString = foreignDB.getDbName().toString();
        return abbrevString.contains("miRNA") ||
                abbrevString.contains("miRBASE")
                ;
    }

    public boolean isRefSeq() {
        // would better put the hard=coded ids to somewhere else as Enum values
        return (getZdbID().equals("ZDB-FDBCONT-040412-38") || getZdbID().equals("ZDB-FDBCONT-040412-39") || getZdbID().equals("ZDB-FDBCONT-040527-1"));
    }

    public boolean isValidAccessionFormat(String accessionNo) {
        return getValidationFailedMessage(accessionNo).isEmpty();
    }

    public Optional<String> getValidationFailedMessage(String accessionNo) {
        Set<ReferenceDatabaseValidationRule> rules = getValidationRules();
        if (rules == null || rules.isEmpty()) {
            return Optional.empty();
        }
        for (ReferenceDatabaseValidationRule rule : rules) {
            if (!rule.isAccessionFormatValid(accessionNo)) {
                return Optional.of(rule.getRuleDescription());
            }
        }
        return Optional.empty();
    }
}


