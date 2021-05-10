package org.zfin.zebrashare;

import org.hibernate.annotations.Type;
import org.zfin.feature.Feature;
import org.zfin.profile.Person;

import javax.persistence.*;
import java.util.Arrays;
import java.util.GregorianCalendar;

@Entity
@Table(name = "feature_community_contribution")
public class FeatureCommunityContribution {

    public enum FunctionalConsequence {
        NULL("Null"),
        HYPOMORPH("Hypomorph"),
        OTHER("Other"),
        UNKNOWN("Unknown");

        private String display;

        FunctionalConsequence(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }

        @Override
        public String toString() {
            return display;
        }

        public static FunctionalConsequence fromString(String value) {
            return Arrays.stream(values()).filter(n -> n.display.equals(value)).findAny().orElse(null);
        }
    }
    public enum NMDApparent {
        Yes("Yes"),
        NO("No"),
        PARTIAL("Partial"),
        NOTASSAYED("Not Assayed");

        private String display;

        NMDApparent(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }

        @Override
        public String toString() {
            return display;
        }

        public static NMDApparent fromString(String value) {
            return Arrays.stream(values()).filter(n -> n.display.equals(value)).findAny().orElse(null);
        }
    }

    @Id
    @Column(name = "fcc_pk_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "fcc_feature_zdb_id")
    private Feature feature;

    @Column(name = "fcc_functional_consequence")
    @Type(type = "org.zfin.framework.StringEnumValueUserType", parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value="org.zfin.zebrashare.FeatureCommunityContribution$FunctionalConsequence")})
    private FunctionalConsequence functionalConsequence;

    @Column(name = "fcc_nmd_apparent")
    @Type(type = "org.zfin.framework.StringEnumValueUserType", parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value="org.zfin.zebrashare.FeatureCommunityContribution$NMDApparent")})
    private NMDApparent nmdApparent;

    @Column(name = "fcc_adult_viable")
    private Boolean adultViable;

    @Column(name = "fcc_maternal_zygosity_examined")
    private Boolean maternalZygosityExamined;

    @Column(name = "fcc_currently_available")
    private Boolean currentlyAvailable;

    @Column(name = "fcc_other_line_information")
    private String otherLineInformation;

    @Column(name = "fcc_date_added")
    private GregorianCalendar date;

    @ManyToOne
    @JoinColumn(name = "fcc_added_by")
    private Person submitter;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public FunctionalConsequence getFunctionalConsequence() {
        return functionalConsequence;
    }

    public void setFunctionalConsequence(FunctionalConsequence functionalConsequence) {
        this.functionalConsequence = functionalConsequence;
    }

    public Boolean getAdultViable() {
        return adultViable;
    }

    public void setAdultViable(Boolean adultViable) {
        this.adultViable = adultViable;
    }

    public Boolean getMaternalZygosityExamined() {
        return maternalZygosityExamined;
    }

    public void setMaternalZygosityExamined(Boolean maternalZygosityExamined) {
        this.maternalZygosityExamined = maternalZygosityExamined;
    }

    public Boolean getCurrentlyAvailable() {
        return currentlyAvailable;
    }

    public void setCurrentlyAvailable(Boolean currentlyAvailable) {
        this.currentlyAvailable = currentlyAvailable;
    }

    public String getOtherLineInformation() {
        return otherLineInformation;
    }

    public void setOtherLineInformation(String otherLineInformation) {
        this.otherLineInformation = otherLineInformation;
    }

    public GregorianCalendar getDate() {
        return date;
    }

    public void setDate(GregorianCalendar date) {
        this.date = date;
    }

    public Person getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Person submitter) {
        this.submitter = submitter;
    }

    public NMDApparent getNmdApparent() {
        return nmdApparent;
    }

    public void setNmdApparent(NMDApparent nmdApparent) {
        this.nmdApparent = nmdApparent;
    }

    @Override
    public String toString() {
        return "FeatureCommunityContribution{" +
                "id=" + id +
                ", feature=" + feature +
                ", functionalConsequence=" + functionalConsequence +
                ", nmdApparent=" + nmdApparent +
                ", adultViable=" + adultViable +
                ", maternalZygosityExamined=" + maternalZygosityExamined +
                ", currentlyAvailable=" + currentlyAvailable +
                ", otherLineInformation='" + otherLineInformation + '\'' +
                ", date=" + date +
                ", submitter=" + submitter +
                '}';
    }
}
