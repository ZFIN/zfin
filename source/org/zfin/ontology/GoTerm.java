package org.zfin.ontology;

import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * domain object for GO ontology. Will be superseded by the gDAG.
 */
public class GoTerm implements Term, Comparable<GoTerm> {

    private String zdbID;
    private String name;
    private String oboID;
    private String subOntology;
    private boolean obsolete;

    private List<TermRelationship> relationships;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return zdbID;
    }

    public void setID(String id) {
        // Todo when GO goes into term table
    }

    public String getTermName() {
        return name;
    }

    public void setTermName(String termName) {
        // Todo when GO goes into term table
    }

    public void setOntology(Ontology ontology) {
        // ignore for now
    }

    public Ontology getOntology() {
        if(subOntology.equals("Molecular Function"))
            return Ontology.GO_MF;
        if(subOntology.equals("Cellular Component"))
            return Ontology.GO_CC;
        if(subOntology.equals("Biological Process"))
            return Ontology.GO_BP;
        return null;
    }

    public String getOboID() {
        return oboID;
    }

    public void setOboID(String oboID) {
        this.oboID = oboID;
    }

    public String getComment() {
        return null;
    }

    public void setComment(String comment) {
        
    }

    public String getSubOntology() {
        return subOntology;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public boolean isRoot() {
        return false;
    }

    public void setRoot(boolean root) {
        // Todo when GO goes into term table
    }

    public boolean isSecondary() {
        return false;
    }

    public void setSecondary(boolean secondary) {
        // Todo when GO goes into term table
    }

    public Set<TermAlias> getAliases() {
        return null;
    }

    public void setAliases(Set<TermAlias> aliases) {
        
    }

    public String getDefinition() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setDefinition(String definition) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public void setSubOntology(String subOntology) {
        this.subOntology = subOntology;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoTerm)) return false;

        GoTerm goTerm = (GoTerm) o;

        if (zdbID != null &&  goTerm.zdbID != null) {
            return zdbID.equals(goTerm.zdbID) ;
        }

        if (name != null ? !name.equals(goTerm.name) : goTerm.name != null) return false;
        if (oboID != null ? !oboID.equals(goTerm.oboID) : goTerm.oboID != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = zdbID != null ? zdbID.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (oboID != null ? oboID.hashCode() : 0);
        return result;
    }

    public int compareTo(GoTerm goTerm) {
        if (goTerm == null)
            return 1;
        return name.compareTo(goTerm.getName());
    }

    public List<TermRelationship> getRelatedTerms() {
        if (relationships != null)
            return relationships;

        relationships = new ArrayList<TermRelationship>();
        OntologyRepository ontolgoyOntologyRepository = RepositoryFactory.getOntologyRepository();
        relationships.addAll(ontolgoyOntologyRepository.getTermRelationships(this));
        return relationships;
    }

    /**
     * Retrieves all terms that are immediate children of this term.
     * @return list of children terms
     */
    public List<Term> getChildrenTerms(){
        // ToDo: To be implemented
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GoTerm");
        sb.append("{zdbID='").append(zdbID).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", oboID='").append(oboID).append('\'');
        sb.append(", subOntology='").append(subOntology).append('\'');
        sb.append(", obsolete=").append(obsolete);
        sb.append(", relationships=");
        if(relationships!=null){
            for(TermRelationship termRelationship : relationships){
                sb.append(termRelationship.getID()) ;
            }
        }
        sb.append('}');
        return sb.toString();
    }
}
