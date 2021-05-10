package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VariantDTO {

    private String alleleId;
    private String assembly;
    private String chromosome;
    private Integer start;
    private Integer end;
    private String sequenceOfReferenceAccessionNumber;
    private String genomicReferenceSequence;
    private String genomicVariantSequence;
    private String genomicVariantSequenceAccessionNumber;
    private String type;
    private String consequence;
    private List<NoteDTO> notes;

    public List<PublicationAgrDTO> getReferences() {
        return references;
    }

    private List<PublicationAgrDTO> references;


    public List<NoteDTO> getNotes() {
        return notes;
    }

    public void setNotes(List<NoteDTO> notes) {
        this.notes = notes;
    }

    public List<String> getNote() {

        return note;
    }

    public void setReferences(List<PublicationAgrDTO> references) {
        this.references = references;
    }

    public void setNote(List<String> note) {
        this.note = note;
    }



    private List<String> note;
    private List<CrossReferenceDTO> crossReferences;



    public String getAlleleId() {
        return alleleId;
    }

    public void setAlleleId(String alleleId) {
        this.alleleId = alleleId;
    }

    public String getAssembly() {
        return assembly;
    }

    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public String getSequenceOfReferenceAccessionNumber() {
        return sequenceOfReferenceAccessionNumber;
    }

    public void setSequenceOfReferenceAccessionNumber(String sequenceOfReferenceAccessionNumber) {
        this.sequenceOfReferenceAccessionNumber = sequenceOfReferenceAccessionNumber;
    }

    public String getGenomicReferenceSequence() {
        return genomicReferenceSequence;
    }

    public void setGenomicReferenceSequence(String genomicReferenceSequence) {
        this.genomicReferenceSequence = genomicReferenceSequence;
    }

    public String getGenomicVariantSequence() {
        return genomicVariantSequence;
    }
    

    public List<CrossReferenceDTO> getCrossReferences() {
        return crossReferences;
    }

    public void setCrossReferences(List<CrossReferenceDTO> crossReferences) {
        this.crossReferences = crossReferences;
    }

    public void setGenomicVariantSequence(String genomicVariantSequence) {
        this.genomicVariantSequence = genomicVariantSequence;
    }

    public String getGenomicVariantSequenceAccessionNumber() {
        return genomicVariantSequenceAccessionNumber;
    }

    public void setGenomicVariantSequenceAccessionNumber(String genomicVariantSequenceAccessionNumber) {
        this.genomicVariantSequenceAccessionNumber = genomicVariantSequenceAccessionNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConsequence() {
        return consequence;
    }

    public void setConsequence(String consequence) {
        this.consequence = consequence;
    }






}
