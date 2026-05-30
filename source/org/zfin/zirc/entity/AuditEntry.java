package org.zfin.zirc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.Date;

/**
 * One row in the ZIRC audit trail. Written from
 * {@code ZircSubmissionService} alongside every field-path PATCH, every
 * add/delete on a child aggregate, and every attachment upload/delete.
 *
 * <p>{@code oldValue} and {@code newValue} are stored as JSONB strings so the
 * existing Jackson round-trip in the service stays unchanged; queries that
 * need to dig into a specific field can use Postgres JSON operators on the
 * column.
 */
@Entity(name = "ZircAuditEntry")
@Table(schema = "zirc", name = "audit")
@Getter
@Setter
public class AuditEntry implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ae_id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "ae_actor", nullable = false)
    private String actor;

    @Column(name = "ae_entity_kind", nullable = false)
    private String entityKind;

    @Column(name = "ae_entity_id", nullable = false)
    private String entityId;

    @Column(name = "ae_action", nullable = false)
    private String action;

    @Column(name = "ae_path")
    private String path;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ae_old_value")
    private String oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ae_new_value")
    private String newValue;

    @Column(name = "ae_at", insertable = false, updatable = false)
    private Date at;

}
