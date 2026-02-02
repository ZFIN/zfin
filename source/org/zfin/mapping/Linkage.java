package org.zfin.mapping;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.infrastructure.ActiveSource;
import org.zfin.infrastructure.ZdbID;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "linkage")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
        "CASE get_obj_type(lnkg_source_zdb_id) " +
        "WHEN 'PUB' THEN 'Pub' " +
        "ELSE 'Per' " +
        "END"
)
@Getter
@Setter
public class Linkage {

    @Id
    @GeneratedValue(generator = "Linkage")
    @GenericGenerator(name = "Linkage",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "LINK"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "lnkg_zdb_id")
    protected String zdbID;

    @Transient
    protected long id;

    @Column(name = "lnkg_chromosome", nullable = false)
    protected String chromosome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lnkg_submitter_zdb_id")
    protected Person person;

    @Column(name = "lnkg_comments", nullable = false)
    protected String comments;

    @OneToMany(mappedBy = "linkage", cascade = CascadeType.ALL, orphanRemoval = true)
    protected Set<LinkageMember> linkageMemberSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lnkg_source_zdb_id", insertable = false, updatable = false)
    private Publication publication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lnkg_source_zdb_id", insertable = false, updatable = false)
    private Person personReference;

    @Column(name = "lnkg_source_zdb_id", insertable = false, updatable = false)
    private String referenceID;

    public String getChromosome() {
        if (chromosome != null && chromosome.equals("0"))
            return "unknown";
        return chromosome;
    }

    public ZdbID getReference() {
        if (ActiveSource.isValidActiveData(referenceID, ActiveSource.Type.PUB))
            return publication;
        else
            return personReference;
    }

    public void addLinkageMember(LinkageMember member) {
        if (linkageMemberSet == null)
            linkageMemberSet = new HashSet<>(4);
        linkageMemberSet.add(member);
        member.setLinkage(this);
        // add inverse linkage entity
        linkageMemberSet.add(member.getInverseMember());
    }

}
