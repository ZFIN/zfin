package org.zfin.mutant;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.util.CollectionUtils;
import org.zfin.fish.FishAnnotation;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.marker.Marker;
import org.zfin.profile.GenotypeSupplier;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import jakarta.persistence.*;
import java.util.*;
@Setter
@Getter
@Table(name = "strain")
public class Strain  {

    @Column(name = "geno_zdb_id")
    private String zdbID;
    @Column(name = "geno_display_name")
    private String name;
}
