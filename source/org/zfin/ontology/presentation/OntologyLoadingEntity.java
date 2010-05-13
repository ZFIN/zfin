package org.zfin.ontology.presentation;

import org.zfin.ontology.Ontology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * This convenience class holds the information about loading an ontology.
 * It contains the last loaded data as an attribute, LoadingData, and previous
 * loads in a collection.
 */
public class OntologyLoadingEntity implements Serializable {

    private Ontology ontology;
    private LoadingData lastLoad;
    private static final int MAXIMUM_NUMBER_OF_HISTORIC_LOADS = 10;
    private Collection<LoadingData> allLoadingEvents = new ArrayList<LoadingData>(MAXIMUM_NUMBER_OF_HISTORIC_LOADS);

    public OntologyLoadingEntity(Ontology ontology) {
        this.ontology = ontology;
    }

    /**
     * Only keep the last 10 records.
     *
     * @param dateOfLoad          date of the load
     * @param loadingTime         time it took to load the ontology
     * @param numOfTerms          number of terms in the ontology
     * @param numOfObsoletedTerms number of terms that are marked obsolete
     * @param numOfAliases        number of aliases
     */
    public void addLoadingEvent(Date dateOfLoad, long loadingTime, int numOfTerms, int numOfObsoletedTerms, int numOfAliases) {
        allLoadingEvents.add(lastLoad);
        lastLoad = new LoadingData(dateOfLoad, loadingTime, numOfTerms, numOfObsoletedTerms, numOfAliases);
        if (allLoadingEvents.size() > MAXIMUM_NUMBER_OF_HISTORIC_LOADS)
            allLoadingEvents.remove(0);
    }

    public Ontology getOntology() {
        return ontology;
    }

    public int getNumberOfTerms() {
        return lastLoad.getNumberOfTerms();
    }

    public Date getDateLastLoaded() {
        return lastLoad.getDateLastLoaded();
    }

    public double getTimeLastLoaded() {
        return (double) lastLoad.getLoadingTime() / 1000.0;
    }

    public LoadingData getLastLoad() {
        return lastLoad;
    }

    public Collection<LoadingData> getAllLoadingEvents() {
        return allLoadingEvents;
    }

}
