package org.zfin.infrastructure.submission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * A record of one submission through a public form, whether we accepted it or threw it away.
 * <p>
 * Rejected submissions are kept deliberately: the spam heuristics in
 * {@link org.zfin.infrastructure.spam.SpamDetector} can only be tuned if we can see what they
 * caught, and a legitimate request that went missing can be recovered from here.
 */
@Entity
@Table(name = "submission_log")
@Getter
@Setter
public class SubmissionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sublog_pk_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "sublog_type", nullable = false)
    private SubmissionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "sublog_outcome", nullable = false)
    private SubmissionOutcome outcome;

    @Column(name = "sublog_date", nullable = false)
    private Date date = new Date();

    @Column(name = "sublog_spam_score", nullable = false)
    private int spamScore;

    /**
     * Which heuristics fired, so the log explains itself without a code read.
     */
    @Column(name = "sublog_spam_reasons")
    private String spamReasons;

    /**
     * Why a submission was returned to the submitter, when it was.
     */
    @Column(name = "sublog_validation_errors")
    private String validationErrors;

    @Column(name = "sublog_name")
    private String name;

    @Column(name = "sublog_email")
    private String email;

    @Column(name = "sublog_orcid")
    private String orcid;

    // Person submissions are stored field by field as well as in the details text, so a coordinator
    // creating the account can have the form prefilled from the request rather than retyping it.

    @Column(name = "sublog_first_name")
    private String firstName;

    @Column(name = "sublog_last_name")
    private String lastName;

    @Column(name = "sublog_address")
    private String address;

    @Column(name = "sublog_country")
    private String country;

    @Column(name = "sublog_phone")
    private String phone;

    @Column(name = "sublog_lab")
    private String lab;

    @Column(name = "sublog_role")
    private String role;

    @Column(name = "sublog_url")
    private String url;

    @Column(name = "sublog_comments")
    private String comments;

    /**
     * The whole submission as the coordinator would see it, so no field is lost to review.
     */
    @Column(name = "sublog_details")
    private String details;

    @Column(name = "sublog_ip_address")
    private String ipAddress;

    @Column(name = "sublog_user_agent")
    private String userAgent;
}
