package org.zfin.sequence;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.regex.Pattern;

@Getter
@Setter
@Entity
@Table(name = "foreign_db_contains_validation_rule")
public class ReferenceDatabaseValidationRule {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "fdbcvr_pk_id")
        private String ruleID;
        @Column(name = "fdbcvr_rule_name")
        private String ruleName;

        @Column(name = "fdbcvr_rule_description")
        private String ruleDescription;

        @Column(name = "fdbcvr_rule_reference_url")
        private String ruleReferenceURL;

        @Column(name = "fdbcvr_rule_pattern")
        private String rulePattern;

        @ManyToOne
        @JoinColumn(name = "fdbcvr_fdbcont_zdb_id", nullable = false)
        private ReferenceDatabase referenceDatabase;

        public boolean isAccessionFormatValid(String accessionNo) {
                if (rulePattern == null || rulePattern.isEmpty()) {
                        return true;
                }
                return Pattern.compile(rulePattern).matcher(accessionNo).matches();
        }
}
