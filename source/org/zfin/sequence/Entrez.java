package org.zfin.sequence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "entrez_gene")
@Setter
@Getter
public class Entrez {

    @Id
    @Column(name = "eg_acc_num", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String entrezAccNum;

    @Column(name = "eg_symbol")
    private String abbreviation;

    @Column(name = "eg_name")
    private String name;

    @OneToMany(mappedBy = "entrezAccession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<EntrezOMIM> relatedOMIMAccessions;

    @OneToMany(mappedBy = "entrezAccession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<EntrezMGI> relatedMGIAccessions;

    @OneToMany(mappedBy = "entrezAccession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<EntrezProtRelation> relatedProteinAccessions;

}
