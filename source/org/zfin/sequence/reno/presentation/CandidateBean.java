package org.zfin.sequence.reno.presentation;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.log4j.Logger;
import org.zfin.marker.Marker;
import org.zfin.orthology.OrthoEvidence;
import org.zfin.people.Person;
import org.zfin.sequence.Entrez;
import org.zfin.sequence.EntrezProtRelation;
import org.zfin.sequence.reno.RunCandidate;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CandidateBean {
    private static Logger LOG = Logger.getLogger(CandidateBean.class);

    public static final String IGNORE = "IGNORE";
    public static final String NOVEL = "NOVEL";

    public static final String NEW_GENE_NAME = "geneName";
    public static final String NEW_ABBREVIATION = "geneAbbreviation";
    public static final String HUMAN_ORTHOLOGY_EVIDENCE = "humanOrthologyEvidence";
    public static final String MOUSE_ORTHOLOGY_EVIDENCE = "mouseOrthologyEvidence";

    public static final String BLAST_URL = "http://zfin.org/blast/blastit.cgi?PROGRAM=blastp&DATALIB=refseq_zf_aa&DATALIB=sptr_zf&SEQ_TYPE=pt&EXPECT=10&WORD=3&MATRIX=BLOSUM62&SEG=on&XNU=on&GRAPH_DISPLAY=on&SEQ_ID=";
    public static final String ZEBRAFISH = "Zebrafish";

    public static final String SET_PROBLEM = "SET_PROBLEM";
    public static final String SAVE_NOTE = "savenote";
    public static final String LOCK_RECORD = "lock";
    public static final String UNLOCK_RECORD = "unlock";
    public static final String DONE = "done";

    RunCandidate runCandidate;

    private String associatedGeneField;
    private Collection<Marker> allSingleAssociatedGenesFromQueries;
    private String geneName;
    private String geneAbbreviation;
    private String geneFamilyName;
    private String geneZdbID;
    private String message;
    private List<Marker> smallSegments;

    //flag for to signal that the gene is being renamed
    private boolean rename;

    private String action;

    private String candidateNote;
    private boolean candidateProblem;

    private String nomenclaturePublicationZdbID;  // used by both run types
    private String orthologyPublicationZdbID;  // used by nomenclature run only
    private String relationPublicationZdbID; // attribute used by redundancy only


    private EntrezProtRelation humanOrthologueAbbrev;

    public String getRelationPublicationZdbID() {
        return relationPublicationZdbID;
    }

    public void setRelationPublicationZdbID(String relationPublicationZdbID) {
        this.relationPublicationZdbID = relationPublicationZdbID;
    }

    private Set<OrthoEvidence.Code> humanOrthologyEvidence;

    private EntrezProtRelation mouseOrthologueAbbrev;
    private Set<OrthoEvidence.Code> mouseOrthologyEvidence;

    public void setCandidateNote(String candidateNote) {
        this.candidateNote = candidateNote;
    }

    public String getCandidateNote() {
        return candidateNote;
    }


    public boolean isCandidateProblem() {
        return candidateProblem;
    }

    public void setCandidateProblem(boolean candidateProblem) {
        this.candidateProblem = candidateProblem;
    }

    public EntrezProtRelation getHumanOrthologueAbbrev() {
        if (humanOrthologueAbbrev == null) {
            humanOrthologueAbbrev = new EntrezProtRelation();
            Entrez humanEntrez = new Entrez();
            humanOrthologueAbbrev.setEntrezAccession(humanEntrez);
        }
        return humanOrthologueAbbrev;
    }

    public void setHumanOrthologueAbbrev(EntrezProtRelation humanOrthologueAbbrev) {
        this.humanOrthologueAbbrev = humanOrthologueAbbrev;
    }


    public Set<OrthoEvidence.Code> getHumanOrthologyEvidence() {
        return humanOrthologyEvidence;
    }

    public void setHumanOrthologyEvidence(Set<OrthoEvidence.Code> humanOrthologyEvidence) {
        this.humanOrthologyEvidence = humanOrthologyEvidence;
    }

    public Set<OrthoEvidence.Code> getMouseOrthologyEvidence() {

        return mouseOrthologyEvidence;
    }

    public void setMouseOrthologyEvidence(Set<OrthoEvidence.Code> mouseOrthologyEvidence) {
        this.mouseOrthologyEvidence = mouseOrthologyEvidence;
    }

    public EntrezProtRelation getMouseOrthologueAbbrev() {
        if (mouseOrthologueAbbrev == null) {
            mouseOrthologueAbbrev = new EntrezProtRelation();
            Entrez mouseEntrez = new Entrez();
            mouseOrthologueAbbrev.setEntrezAccession(mouseEntrez);
        }
        return mouseOrthologueAbbrev;
    }

    public void setMouseOrthologueAbbrev(EntrezProtRelation mouseOrthologueAbbrev) {
        this.mouseOrthologueAbbrev = mouseOrthologueAbbrev;
    }

    public String getNomenclaturePublicationZdbID() {
        return nomenclaturePublicationZdbID;
    }

    public void setNomenclaturePublicationZdbID(String nomenclaturePublicationZdbID) {
        this.nomenclaturePublicationZdbID = nomenclaturePublicationZdbID;
    }


    public String getOrthologyPublicationZdbID() {
        return orthologyPublicationZdbID;
    }

    public void setOrthologyPublicationZdbID(String orthologyPublicationZdbID) {
        this.orthologyPublicationZdbID = orthologyPublicationZdbID;
    }

    public RunCandidate getRunCandidate() {
        if (runCandidate == null) {
            runCandidate = new RunCandidate();
        }
        return runCandidate;
    }

    public void setRunCandidate(RunCandidate runCandidate) {
        this.runCandidate = runCandidate;
    }


    public String getAssociatedGeneField() {
        return associatedGeneField;
    }

    public void setAssociatedGeneField(String associatedGeneField) {
        this.associatedGeneField = associatedGeneField;
    }


    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getGeneAbbreviation() {
        return geneAbbreviation;
    }

    public void setGeneAbbreviation(String geneAbbreviation) {
        this.geneAbbreviation = geneAbbreviation;
    }

    public String getGeneFamilyName() {
        return geneFamilyName;
    }

    public void setGeneFamilyName(String geneFamilyName) {
        this.geneFamilyName = geneFamilyName;
    }


    public String getGeneZdbID() {
        return geneZdbID;
    }

    public void setGeneZdbID(String geneZdbID) {
        this.geneZdbID = geneZdbID;
    }


    public boolean isRename() {
        return rename;
    }

    public void setRename(boolean rename) {
        this.rename = rename;
    }


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getBlastUrl() {
        return BLAST_URL;
    }


    public static String getSaveNote() {
        return SAVE_NOTE;
    }

    public static String getLockRecord() {
        return LOCK_RECORD;
    }

    public static String getUnlockRecord() {
        return UNLOCK_RECORD;
    }

    public static String getDone() {
        return DONE;
    }

    public Collection<Marker> getAllSingleAssociatedGenesFromQueries() {
        return allSingleAssociatedGenesFromQueries;
    }

    public void setAllSingleAssociatedGenesFromQueries(Collection<Marker> allSingleAssociatedGenesFromQueries) {
        this.allSingleAssociatedGenesFromQueries = allSingleAssociatedGenesFromQueries;
    }

    /**
     * Warning message that the candidate is already related to any of the associated genes
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Warning message that the candidate is already related to any of the associated genes 
     * @param message string
     */
    public void addMessage(String message) {
        if (this.message == null)
            this.message = message;
        else
            this.message += message;
        this.message += " ";
    }

    public List<Marker> getSmallSegments() {
        return smallSegments;
    }

    public void setSmallSegments(List<Marker> smallSegments) {
        this.smallSegments = smallSegments;
    }

    /**
     * Check if there is a message used in case the candidate is laready related to any of the associated genes.
     * @return boolean
     */
    public boolean isAlreadyAssociatedGenes(){
        return message != null;
    }

    /**
     * Is this candidate selected as a novel gene?
     *
     * @return boolean
     */
    public boolean isNovelGene() {
        return associatedGeneField != null && associatedGeneField.equals(NOVEL);
    }


    public EntrezProtRelation getTargetAccessionHuman(RunCandidate rc, String accName) {
        LOG.debug("enter getTargetAccessionHuman");
        Set<EntrezProtRelation> humanAccOrthologs = rc.getHumanOrthologuesFromQueries();
        LOG.debug("returned humanAccOrthologues: ");
        for (EntrezProtRelation humanAccOrtholog : humanAccOrthologs) {
            LOG.debug("for each humanAccOrtholog");
            if (humanAccOrtholog.getEntrezAccession().getEntrezAccNum().equals(accName)) {
                return humanAccOrtholog;

            }
        }
        return null;
    }

    public EntrezProtRelation getTargetAccessionMouse(RunCandidate rc, String accName) {
        Set<EntrezProtRelation> mouseAccOrthologs = rc.getMouseOrthologuesFromQueries();
        for (EntrezProtRelation mouseAccOrtholog : mouseAccOrthologs) {
            if (mouseAccOrtholog.getEntrezAccession().getEntrezAccNum().equals(accName)) {
                return mouseAccOrtholog;

            }
        }
        return null;
    }

    public Person getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Person) authentication.getPrincipal();
    }

    public boolean isOwnerViewing() {
        if (getRunCandidate().getLockPerson() != null
                && getCurrentUser().equals(getRunCandidate().getLockPerson())) {
            return true;
        } else {
            return false;
        }

    }
}
       
