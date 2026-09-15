package org.zfin.zirc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.zfin.profile.Person;

import java.io.Serializable;
import java.util.Date;

/**
 * One uploaded image attached to a {@link Phenotype} (ZFIN-10449).
 *
 * <p>Mirrors {@link GenotypingAssayFile} field for field, minus its
 * {@code kind} column: an assay surfaces a different attachment heading per
 * assay type, while a phenotype has exactly one bucket ("Phenotype images"),
 * so a kind column would only ever hold one value.
 *
 * <p>{@code storedPath} is a server-constructed absolute path, never anything
 * the client supplied — see {@code ZircSubmissionService.storePhenotypeAttachment}.
 */
@Entity(name = "ZircPhenotypeFile")
@Table(schema = "zirc", name = "phenotype_file")
@Getter
@Setter
public class PhenotypeFile implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pf_id", updatable = false, nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pf_phenotype_id", referencedColumnName = "p_id", nullable = false)
    private Phenotype phenotype;

    @Column(name = "pf_original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "pf_stored_path", nullable = false)
    private String storedPath;

    @Column(name = "pf_content_type")
    private String contentType;

    @Column(name = "pf_file_size")
    private Long fileSize;

    @Column(name = "pf_uploaded_at", insertable = false, updatable = false)
    private Date uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pf_uploaded_by", referencedColumnName = "zdb_id")
    private Person uploadedBy;

}
