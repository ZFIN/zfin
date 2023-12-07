package org.zfin.sequence;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class ReferenceDatabaseValidationRule {

        private String ruleID;
        private String ruleName;
        private String ruleDescription;
        private String ruleReferenceURL;
        private String rulePattern;
        private ReferenceDatabase referenceDatabase;

        public boolean isAccessionFormatValid(String accessionNo) {
                if (rulePattern == null || rulePattern.isEmpty()) {
                        return true;
                }
                return Pattern.compile(rulePattern).matcher(accessionNo).matches();
        }
}
