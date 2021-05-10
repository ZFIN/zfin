package org.zfin.zebrashare;

import org.zfin.profile.Lab;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "zebrashare_submission_metadata")
public class ZebrashareSubmissionMetadata implements Serializable {

    @Id
    @MapsId
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "zsm_pub_zdb_id")
    private Publication publication;

    // this is the account that was logged in while submitting
    @ManyToOne
    @JoinColumn(name = "zsm_submitter_zdb_id")
    private Person submitter;

    // this is the contact info they provided in the form
    @Column(name = "zsm_submitter_name")
    private String submitterName;

    @Column(name = "zsm_submitter_email")
    private String submitterEmail;

    @ManyToOne
    @JoinColumn(name = "zsm_lab_of_origin_zdb_id")
    private Lab labOfOrigin;

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Person getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Person submitter) {
        this.submitter = submitter;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public void setSubmitterEmail(String submitterEmail) {
        this.submitterEmail = submitterEmail;
    }

    public Lab getLabOfOrigin() {
        return labOfOrigin;
    }

    public void setLabOfOrigin(Lab labOfOrigin) {
        this.labOfOrigin = labOfOrigin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZebrashareSubmissionMetadata that = (ZebrashareSubmissionMetadata) o;
        return Objects.equals(publication, that.publication) &&
                Objects.equals(submitter, that.submitter) &&
                Objects.equals(submitterName, that.submitterName) &&
                Objects.equals(submitterEmail, that.submitterEmail) &&
                Objects.equals(labOfOrigin, that.labOfOrigin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publication, submitter, submitterName, submitterEmail, labOfOrigin);
    }
}
