 package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.zfin.framework.api.View;
import org.zfin.mutant.GoEvidenceCode;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by kschaper on 12/16/14.
 */
public class MarkerGoViewTableRow implements Comparable {
    @JsonView(View.API.class)
    private String id;

    String ontology;
    @JsonView(View.API.class)
    String qualifier;
    @JsonView(View.API.class)
    GenericTerm term;
    @JsonView(View.API.class)
    GoEvidenceCode evidenceCode;
    @JsonView(View.API.class)
    String inferredFrom;
    @JsonView(View.API.class)
    String annotExtns;
    Set<Publication> publications;
    @JsonView(View.API.class)
    String referencesLink;
    String inferredFromAsString;
    String firstInference;

    public MarkerGoViewTableRow(MarkerGoTermEvidence evidence) {
        setId(evidence.getZdbID());
        setOntology(evidence.getGoTerm().getOntology().getCommonName().replace("GO: ", ""));
        if (evidence.getFlag() != null) {
            setQualifier(evidence.getFlag().toString().toUpperCase());
        }
        setTerm(evidence.getGoTerm());
        setEvidenceCode(evidence.getEvidenceCode());

        publications = new TreeSet<>();
        publications.add(evidence.getSource());

        inferredFromAsString = StringUtils.join(evidence.getInferencesAsString(), ", ");
        if (CollectionUtils.isNotEmpty(evidence.getInferencesAsString())) {
            firstInference = evidence.getInferencesAsString().iterator().next();
        }


    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOntology() {
        return ontology;
    }

    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }

    public GoEvidenceCode getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(GoEvidenceCode evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    public String getInferredFrom() {
        return inferredFrom;
    }

    public void setInferredFrom(String inferredFrom) {
        this.inferredFrom = inferredFrom;
    }

    public Set<Publication> getPublications() {
        return publications;
    }

    public void setPublications(Set<Publication> publications) {
        this.publications = publications;
    }

    public void addPublication(Publication publication) {
        publications.add(publication);
    }

    public String getReferencesLink() {
        return referencesLink;
    }

    public void setReferencesLink(String referencesLink) {
        this.referencesLink = referencesLink;
    }

    public String getInferredFromAsString() {
        return inferredFromAsString;
    }

    public void setInferredFromAsString(String inferredFromAsString) {
        this.inferredFromAsString = inferredFromAsString;
    }

    public String getFirstInference() {
        return firstInference;
    }

    public void setFirstInference(String firstInference) {
        this.firstInference = firstInference;
    }

    public String getAnnotExtns() {
        return annotExtns;
    }

    public void setAnnotExtns(String annotExtns) {
        this.annotExtns = annotExtns;
    }

    @Override
    public int compareTo(Object o) {
        MarkerGoViewTableRow other = (MarkerGoViewTableRow) o;

        if (other == null) {
            return 1;
        }

        //reverse sorting this one
        int i = ObjectUtils.compare(other.getOntology(), getOntology());
        if (i != 0) {
            return i;
        }

        i = ObjectUtils.compare(getTerm(), other.getTerm());
        if (i != 0) {
            return i;
        }

        i = ObjectUtils.compare(getQualifier(), other.getQualifier());
        if (i != 0) {
            return i;
        }

        i = ObjectUtils.compare(getEvidenceCode().getName(), other.getEvidenceCode().getName());
        if (i != 0) {
            return i;
        }

        i = ObjectUtils.compare(getInferredFrom(), other.getInferredFrom());
        if (i != 0) {
            return i;
        }

        i = ObjectUtils.compare(getAnnotExtns(), other.getAnnotExtns());
        if (i != 0) {
            return i;
        }
        return 0;
    }

    public boolean equals(Object o) {
        return compareTo(o) == 0;
    }
}
