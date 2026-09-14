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
import java.util.Set;

/**
 * One uploaded file attached to a {@link GenotypingAssay}. {@code af_kind}
 * says which of the form's two upload buckets the file belongs to — see
 * {@link #KIND_ASSAY_RESULT} and {@link #KIND_PROTOCOL_DOC}.
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

    /**
     * The per-assay-type results bucket — rendered as "Annotated gel images",
     * "Chromatograms", "Annotated result images" or "Annotated melt curve
     * files" depending on {@code ga_assay_type}. Which of those four labels
     * applies is derived from the assay type at render time, so it is not
     * stored per file.
     */
    public static final String KIND_ASSAY_RESULT = "assay_result";

    /** Protocol documentation (ZFIN-10415); offered for every assay type. */
    public static final String KIND_PROTOCOL_DOC = "protocol_doc";

    /** The kinds {@code af_kind}'s CHECK constraint permits. */
    public static final Set<String> KINDS = Set.of(KIND_ASSAY_RESULT, KIND_PROTOCOL_DOC);

    // Which upload bucket this file belongs to; one of KINDS. NOT NULL since
    // ZFIN-10415 — a kind-less row belongs to no bucket and so would be
    // invisible in the form.
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
