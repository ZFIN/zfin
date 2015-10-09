package org.zfin.sequence.reno.presentation;

import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zfin.marker.Marker;
import org.zfin.orthology.OrthologEvidence;
import org.zfin.profile.Person;
import org.zfin.sequence.*;
import org.zfin.sequence.reno.RunCandidate;

import java.util.*;

public class CandidateBean {
    private static Logger logger = Logger.getLogger(CandidateBean.class);

    public static final String IGNORE = "IGNORE";
    public static final String NOVEL = "NOVEL";

    public static final String NEW_GENE_NAME = "geneName";
    public static final String NEW_ABBREVIATION = "geneAbbreviation";
    public static final String HUMAN_ORTHOLOGY_EVIDENCE = "humanOrthologyEvidence";
    public static final String MOUSE_ORTHOLOGY_EVIDENCE = "mouseOrthologyEvidence";

    public static final String ZEBRAFISH = "Zebrafish";

    public static final String SET_PROBLEM = "SET_PROBLEM";
    public static final String SAVE_NOTE = "savenote";
    public static final String LOCK_RECORD = "lock";
    public static final String UNLOCK_RECORD = "unlock";
    public static final String DONE = "done";

    private RunCandidate runCandidate;

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
    private ReferenceDatabase humanReferenceDatabase;
    private ReferenceDatabase mouseReferenceDatabase;
    private boolean geneAlias;

    private String geneFamilyJson;

    public String getRelationPublicationZdbID() {
        return relationPublicationZdbID;
    }

    public void setRelationPublicationZdbID(String relationPublicationZdbID) {
        this.relationPublicationZdbID = relationPublicationZdbID;
    }

    private Set<OrthologEvidence.Code> humanOrthologyEvidence;

    private EntrezProtRelation mouseOrthologueAbbrev;
    private Set<OrthologEvidence.Code> mouseOrthologyEvidence;

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

    public ReferenceDatabase getHumanReferenceDatabase() {
        return humanReferenceDatabase;
    }

    public void setHumanReferenceDatabase(ReferenceDatabase humanReferenceDatabase) {
        this.humanReferenceDatabase = humanReferenceDatabase;
    }

    public ReferenceDatabase getMouseReferenceDatabase() {
        return mouseReferenceDatabase;
    }

    public void setMouseReferenceDatabase(ReferenceDatabase mouseReferenceDatabase) {
        this.mouseReferenceDatabase = mouseReferenceDatabase;
    }

    public Set<OrthologEvidence.Code> getHumanOrthologyEvidence() {
        return humanOrthologyEvidence;
    }

    public void setHumanOrthologyEvidence(Set<OrthologEvidence.Code> humanOrthologyEvidence) {
        this.humanOrthologyEvidence = humanOrthologyEvidence;
    }

    public Set<OrthologEvidence.Code> getMouseOrthologyEvidence() {

        return mouseOrthologyEvidence;
    }

    public void setMouseOrthologyEvidence(Set<OrthologEvidence.Code> mouseOrthologyEvidence) {
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

    public boolean getSuggestedNameExists() {
        if (allSingleAssociatedGenesFromQueries == null) {
            return false;
        } else {
            Set<String> geneSymbols = new HashSet<>();
            for (Marker m : allSingleAssociatedGenesFromQueries) {
                geneSymbols.add(m.getAbbreviation());
            }

            return geneSymbols.contains(getRunCandidate().getCandidate().getSuggestedName());
        }
    }

    public Collection<Marker> getAllSingleAssociatedGenesFromQueries() {
        return allSingleAssociatedGenesFromQueries;
    }

    public void setAllSingleAssociatedGenesFromQueries(Collection<Marker> allSingleAssociatedGenesFromQueries) {
        this.allSingleAssociatedGenesFromQueries = allSingleAssociatedGenesFromQueries;
    }

    /**
     * Warning message that the candidate is already related to any of the associated genes
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Warning message that the candidate is already related to any of the associated genes
     *
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
     * Check if there is a message used in case the candidate is already related to any of the associated genes.
     *
     * @return boolean
     */
    public boolean isAlreadyAssociatedGenes() {
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
        logger.debug("enter getTargetAccessionHuman");
        Set<EntrezProtRelation> humanAccOrthologs = rc.getHumanOrthologuesFromQueries();
        logger.debug("returned humanAccOrthologues: ");
        for (EntrezProtRelation humanAccOrtholog : humanAccOrthologs) {
            logger.debug("for each humanAccOrtholog");
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
        return getRunCandidate().getLockPerson() != null
                && getCurrentUser().equals(getRunCandidate().getLockPerson());

    }

    public void createRunCandidateForZdbID(String zdbID) {
        if (runCandidate != null) {
            logger.error("run candidate was not null, but still overwriting it: " + runCandidate);
            // releasing from memory
            runCandidate = null;
        }
        runCandidate = new RunCandidate();
        runCandidate.setZdbID(zdbID);
    }

    public boolean isGeneAlias() {
        return geneAlias;
    }

    public void setGeneAlias(boolean geneAlias) {
        this.geneAlias = geneAlias;
    }

    public String getGeneFamilyJson() {
        return geneFamilyJson;
    }

    public void setGeneFamilyJson(String geneFamilyJson) {
        this.geneFamilyJson = geneFamilyJson;
    }

}
       
