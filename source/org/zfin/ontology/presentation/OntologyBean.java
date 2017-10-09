package org.zfin.ontology.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.anatomy.service.AnatomyService;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.SectionVisibility;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.marker.presentation.LinkDisplay;
import org.zfin.mutant.OmimPhenotype;
import org.zfin.ontology.*;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;

import java.util.*;

/**
 * Bean used to view cached ontologies.
 */
public class OntologyBean extends PaginationBean {

    private String action;
    private boolean ontologiesLoaded = true;
    private String ontologyName;
    private Ontology ontology;
    private Set<TermDTO> terms;
    private String termID;
    private GenericTerm term;
    private Map<TermDTO, List<String>> valueMap;
    private TreeMap<String, Set<TermDTO>> keys;
    private List<TransitiveClosure> childrenTransitiveClosureSet;
    private OntologyManager ontologyManager;
    private List<RelationshipPresentation> termRelationships;
    private List<OntologyMetadata> metadataList;
    private Map<String, String> stageListDisplay;



    private Set<OmimPhenotype> omimPheno;
    private Set<TermDBLink> agrDiseaseLinks;
    private SectionVisibility sectionVisibility = new SectionVisibility<OntologyBean.Section>(OntologyBean.Section.class);

    public String getAction() {
        return action;
    }


    public Set<TermDBLink> getAgrDiseaseLinks() {
        return agrDiseaseLinks;
    }

    public void setAgrDiseaseLinks(Set<TermDBLink> agrDiseaseLinks) {
        this.agrDiseaseLinks = agrDiseaseLinks;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOntologyName() {
        return ontologyName;
    }

    public void setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
    }

    public boolean isSerializeOntologies() {
        return action != null && ActionType.SERIALIZE_ONTOLOGIES.toString().equals(action);
    }

    public boolean isLoadOntologiesFromFile() {
        return action != null && ActionType.LOAD_FROM_SERIALIZED_FILE.toString().equals(action);
    }

    public boolean isLoadOntologiesFromDatabase() {
        return action != null && ActionType.LOAD_FROM_DATABASE.toString().equals(action);
    }

    public String getTermID() {
        return termID;
    }

    public void setTermID(String termID) {
        this.termID = termID;
    }

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }

    public ActionType getActionType() {
        if (StringUtils.isEmpty(action))
            return null;
        return ActionType.getActionType(action);
    }

    public boolean isOntologiesLoaded() {
        return ontologiesLoaded;
    }

    public void setOntologiesLoaded(boolean ontologiesLoaded) {
        this.ontologiesLoaded = ontologiesLoaded;
    }

    public List<TermDTO> getOrderedTerms() {
        List<TermDTO> termList = new ArrayList<TermDTO>(terms);
        Collections.sort(termList);
        return termList;
    }

    public Set<TermDTO> getTerms() {
        return terms;
    }

    public void setTerms(Set<TermDTO> terms) {
        this.terms = terms;
    }

    public TreeMap<String, Set<TermDTO>> getKeys() {
        return keys;
    }

    public void setKeys(TreeMap<String, Set<TermDTO>> keys) {
        this.keys = keys;
    }

    public List<TransitiveClosure> getAllChildren() {
        return childrenTransitiveClosureSet;
    }

    public void setAllChildren(List<TransitiveClosure> transitiveClosures) {
        this.childrenTransitiveClosureSet = transitiveClosures;
    }

    public Map<TermDTO, List<String>> getValueMap() {
        return valueMap;
    }

    public void setValueMap(Map<TermDTO, List<String>> valueMap) {
        this.valueMap = valueMap;
    }

    public Ontology getOntology() {
        return ontology;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    public OntologyManager getOntologyManager() {
        return ontologyManager;
    }

    public void setOntologyManager(OntologyManager ontologyManager) {
        this.ontologyManager = ontologyManager;
    }

    public List<OntologyMetadata> getMetadataList() {
        return metadataList;
    }

    public void setMetadataList(List<OntologyMetadata> metadataList) {
        this.metadataList = metadataList;
    }

    public List<RelationshipPresentation> getTermRelationships() {
        return termRelationships;
    }
    public List<OmimPhenotypeDisplay> omimPhenos;


    public void setTermRelationships(List<RelationshipPresentation> termRelationships) {
        this.termRelationships = termRelationships;
    }

    public List<OmimPhenotypeDisplay> getOmimPhenos() {
        return omimPhenos;
    }

    public void setOmimPhenos(List<OmimPhenotypeDisplay> omimPhenos) {
        this.omimPhenos = omimPhenos;
    }

    public Map<String, String> getDisplayStages() {
        if (stageListDisplay != null)
            return stageListDisplay;

        stageListDisplay = AnatomyService.getDisplayStages();
        return stageListDisplay;
    }

    public Set<String> getDistinctRelationshipTypes() {
        List<String> termRelationships;
        if (ontology != null) {
            termRelationships = RepositoryFactory.getOntologyRepository().getAllRelationships(ontology);
        } else {
            termRelationships = RepositoryFactory.getOntologyRepository().getAllRelationships();
        }
        Set<String> relationships = new TreeSet<String>();
        relationships.addAll(termRelationships);
        return relationships;
    }



    public SectionVisibility getSectionVisibility() {
        return sectionVisibility;
    }

    public void setSectionVisibility(SectionVisibility sectionVisibility) {
        this.sectionVisibility = sectionVisibility;
    }

    public static enum Section {
        EXPRESSION,
        PHENOTYPE;

        public static String[] getValues() {
            String[] values = new String[values().length];
            int index = 0;
            for (Section section : values()) {
                values[index++] = section.toString();
            }
            return values;
        }
    }


    public Set<OmimPhenotype> getOmimPheno() {
        return omimPheno;
    }

    public void setOmimPheno(Set<OmimPhenotype> omimPheno) {
        this.omimPheno = omimPheno;
    }

    public static enum ActionType {
        SERIALIZE_ONTOLOGIES,
        LOAD_FROM_DATABASE,
        LOAD_FROM_SERIALIZED_FILE,
        SHOW_ALIASES,
        SHOW_EXACT,
        SHOW_OBSOLETE_TERMS,
        SHOW_ALL_TERMS,
        SHOW_TERM,
        SHOW_KEYS,
        SHOW_VALUES,
        SHOW_RELATIONSHIP_TYPES;

        public static ActionType getActionType(String type) {
            for (ActionType t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No action type of string " + type + " found.");
        }

        public String getName() {
            return name();
        }
    }
}
