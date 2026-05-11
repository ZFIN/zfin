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
 * One uploaded file attached to a {@link GenotypingAssay}. {@code af_kind}
 * categorizes the file by its role in the form (chromatogram, gel image,
 * result image, melt curve) — different assay types surface different
 * kinds per the xlsx field matrix.
 */
@Entity(name = "ZircGenotypingAssayFile")
@Table(schema = "zirc", name = "genotyping_assay_file")
@Getter
@Setter
public class GenotypingAssayFile implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "af_id", updatable = false, nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "af_assay_id", referencedColumnName = "ga_id", nullable = false)
    private GenotypingAssay assay;

    @Column(name = "af_kind", nullable = false)
    private String kind;

    @Column(name = "af_original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "af_stored_path", nullable = false)
    private String storedPath;

    @Column(name = "af_content_type")
    private String contentType;

    @Column(name = "af_file_size")
    private Long fileSize;

    @Column(name = "af_uploaded_at", insertable = false, updatable = false)
    private Date uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "af_uploaded_by", referencedColumnName = "zdb_id")
    private Person uploadedBy;

}
