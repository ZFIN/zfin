package org.zfin.profile;

import org.junit.Before;
import org.junit.Test;
import org.zfin.infrastructure.submission.SubmissionLog;
import org.zfin.infrastructure.submission.SubmissionType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SubmissionPrefillTest {

    private SubmissionLog submission;

    @Before
    public void setup() {
        submission = new SubmissionLog();
        submission.setType(SubmissionType.PERSON);
        submission.setFirstName("Frank");
        submission.setLastName("Kim");
        submission.setEmail("gr0750eh@ed.ritsumei.ac.jp");
        submission.setOrcid("0000-0002-1825-0097");
        submission.setPhone("541-346-4607");
        submission.setAddress("1254 University of Oregon");
        submission.setCountry("US");
        submission.setUrl("https://zfin.org");
    }

    @Test
    public void prefillsAnEmptyForm() {
        Person person = new Person();
        assertTrue(SubmissionPrefill.apply(submission, person));

        assertEquals("Frank", person.getFirstName());
        assertEquals("Kim", person.getLastName());
        assertEquals("gr0750eh@ed.ritsumei.ac.jp", person.getEmail());
        assertEquals("0000-0002-1825-0097", person.getOrcidID());
        assertEquals("541-346-4607", person.getPhone());
        assertEquals("1254 University of Oregon", person.getAddress());
        assertEquals("US", person.getCountry());
        assertEquals("https://zfin.org", person.getUrl());
    }

    /**
     * The coordinator may have corrected something before the prefill runs; their value wins.
     */
    @Test
    public void doesNotOverwriteValuesAlreadyEntered() {
        Person person = new Person();
        person.setFirstName("Frances");
        person.setEmail("corrected@example.org");

        assertTrue(SubmissionPrefill.apply(submission, person));

        assertEquals("Frances", person.getFirstName());
        assertEquals("corrected@example.org", person.getEmail());
        assertEquals("Kim", person.getLastName());
    }

    @Test
    public void leavesFieldsTheSubmissionDidNotHave() {
        submission.setPhone(null);
        submission.setUrl("   ");

        Person person = new Person();
        SubmissionPrefill.apply(submission, person);

        assertNull(person.getPhone());
        assertNull(person.getUrl());
    }

    @Test
    public void trimsSubmittedValues() {
        submission.setFirstName("  Frank  ");
        Person person = new Person();
        SubmissionPrefill.apply(submission, person);
        assertEquals("Frank", person.getFirstName());
    }

    /**
     * The organization and user comment forms share the log table but are not account requests.
     */
    @Test
    public void ignoresSubmissionsThatAreNotAccountRequests() {
        submission.setType(SubmissionType.ORGANIZATION);
        Person person = new Person();
        assertFalse(SubmissionPrefill.apply(submission, person));
        assertNull(person.getFirstName());
    }

    @Test
    public void handlesMissingSubmission() {
        Person person = new Person();
        assertFalse(SubmissionPrefill.apply(null, person));
        assertFalse(SubmissionPrefill.apply(new SubmissionLog(), person));
        assertFalse(SubmissionPrefill.apply(submission, null));
    }

    @Test
    public void reportsNothingAppliedWhenSubmissionIsEmpty() {
        SubmissionLog empty = new SubmissionLog();
        empty.setType(SubmissionType.PERSON);
        assertFalse(SubmissionPrefill.apply(empty, new Person()));
    }
}
