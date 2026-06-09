package org.zfin.zirc.entity;

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

import java.util.Date;

/**
 * Per-section curator-approval state for a line submission. One row per
 * (rec_id, section_name). See migration 0070 for the rec_id scheme.
 */
@Entity(name = "ZircLineSubmissionSectionApproval")
@Table(schema = "zirc", name = "line_submission_section_approval")
@Getter
@Setter
public class LineSubmissionSectionApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lssa_pk_id")
    private Long id;

    @Column(name = "lssa_rec_id", nullable = false)
    private String recId;

    @Column(name = "lssa_section_name", nullable = false)
    private String sectionName;

    @Column(name = "lssa_approved", nullable = false)
    private boolean approved;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lssa_approver_zdb_id", referencedColumnName = "zdb_id", nullable = false)
    private Person approver;

    // Last-toggle timestamp. The migration sets a now() default so the
    // first INSERT lands at row creation; subsequent UPDATEs MUST refresh
    // it explicitly (handled by ZircDashboardController.setSectionApproval).
    // Hibernate-side insertable/updatable both true so the explicit set
    // is honored.
    @Column(name = "lssa_approved_at", nullable = false)
    private Date approvedAt;
}
