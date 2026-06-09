package org.zfin.zirc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.zfin.profile.Person;

import java.io.Serializable;
import java.util.Date;

/**
 * Submitter / owner / contact / contributor record on a {@link LineSubmission}.
 * Composite PK: (lineSubmission, person, role) — a person can hold multiple roles
 * on the same submission, but each role appears at most once per person.
 */
@Entity(name = "ZircLineSubmissionPerson")
@Table(schema = "zirc", name = "line_submission_person")
@IdClass(LineSubmissionPersonId.class)
@Getter
@Setter
public class LineSubmissionPerson implements Serializable {

    @JsonIgnore
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lsp_line_submission_id", referencedColumnName = "ls_zdb_id", nullable = false)
    private LineSubmission lineSubmission;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lsp_person_zdb_id", referencedColumnName = "zdb_id", nullable = false)
    private Person person;

    @Id
    @Column(name = "lsp_role", nullable = false)
    private String role;

    @Column(name = "lsp_sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "lsp_created_at", insertable = false, updatable = false)
    private Date createdAt;

}
