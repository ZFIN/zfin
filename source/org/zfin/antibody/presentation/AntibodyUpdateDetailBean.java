package org.zfin.antibody.presentation;

import org.zfin.antibody.Antibody;
import org.zfin.antibody.Isotype;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;
import org.zfin.Species;
import org.zfin.publication.Publication;
import org.zfin.infrastructure.PublicationAttribution;

import java.util.*;

public class AntibodyUpdateDetailBean extends AntibodyBean {
    private String pubAttribData;
    private String supplierName;
    private String newNote;
    private String[] usageNote;
    private String antibodyAliaszdbID;
    private String antibodyAntigenzdbID;
    private String antibodyNotezdbID;
    private String supplierNameErrorString;
    private String supplierzdbID;
    private String antigenpubzdbid;
    private String noteZdbId;
    private String attribution;
    public static final String AB_DEFPUB_ZDB_ID = "antibodyDefPubZdbID";
    public static final String AB_NEW_ANTIGEN_GENE = "newAntigenGene";
    private Marker antigenGene;
    private int usageNoteIndex;
    private int aliasAttribIndex;
    private String aliasRef;
    private String antigenRef;
    private String antibodyDefPubZdbID;
    private String relAttribData;
    private String newAlias;
    private String attribAlias;
    private String newAntigenGene;
    private String attribAntigen;
    private List<Publication> mostRecentPubs;

    public String getSupplierzdbID() {
        return supplierzdbID;
    }

    public void setSupplierzdbID(String supplierzdbID) {
        this.supplierzdbID = supplierzdbID;
    }

    public String getSupplierNameErrorString() {
        return supplierNameErrorString;
    }

    public void setSupplierNameErrorString(String supplierNameErrorString) {
        this.supplierNameErrorString = supplierNameErrorString;
    }

    public String getAntigenpubzdbid() {
        return antigenpubzdbid;
    }

    public void setAntigenpubzdbid(String antigenpubzdbid) {
        this.antigenpubzdbid = antigenpubzdbid;
    }


    public String getAntibodyNotezdbID() {
        return antibodyNotezdbID;
    }

    public void setAntibodyNotezdbID(String antibodyNotezdbID) {
        this.antibodyNotezdbID = antibodyNotezdbID;
    }

    public String getAntibodyAntigenzdbID() {
        return antibodyAntigenzdbID;
    }

    public void setAntibodyAntigenzdbID(String antibodyAntigenzdbID) {
        this.antibodyAntigenzdbID = antibodyAntigenzdbID;
    }

    public String getAntibodyAliaszdbID() {
        return antibodyAliaszdbID;
    }

    public void setAntibodyAliaszdbID(String antibodyAliaszdbID) {
        this.antibodyAliaszdbID = antibodyAliaszdbID;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public int getAliasAttribIndex() {
        return aliasAttribIndex;
    }

    public void setAliasAttribIndex(int aliasAttribIndex) {
        this.aliasAttribIndex = aliasAttribIndex;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }


    public String getAliasRef() {
        return aliasRef;
    }

    public void setAliasRef(String aliasRef) {
        this.aliasRef = aliasRef;
    }


    public String getAntigenRef() {
        return antigenRef;
    }

    public void setAntigenRef(String antigenRef) {
        this.antigenRef = antigenRef;
    }

    public int getUsageNoteIndex() {
        return usageNoteIndex;
    }

    public void setUsageNoteIndex(int usageNoteIndex) {
        this.usageNoteIndex = usageNoteIndex;
    }

    public Marker getAntigenGene() {
        return antigenGene;
    }

    public void setAntigenGene(Marker antigenGene) {
        this.antigenGene = antigenGene;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public String getNoteZdbId() {
        return noteZdbId;
    }

    public void setNoteZdbId(String noteZdbId) {
        this.noteZdbId = noteZdbId;
    }

    public String[] getUsageNote() {
        return usageNote;
    }

    public void setUsageNote(String[] usageNote) {
        this.usageNote = usageNote;
    }

    public String getNewNote() {
        return newNote;
    }

    public void setNewNote(String newNote) {
        this.newNote = newNote;
    }


    public String getAttribAntigen() {
        return attribAntigen;
    }

    public void setAttribAntigen(String attribAntigen) {
        this.attribAntigen = attribAntigen;
    }

    public String getAttribAlias() {
        return attribAlias;
    }

    public void setAttribAlias(String attribAlias) {
        this.attribAlias = attribAlias;
    }

    public String getNewAntigenGene() {
        return newAntigenGene;
    }

    public void setNewAntigenGene(String newAntigenGene) {
        this.newAntigenGene = newAntigenGene;
    }


    public String getAntibodyDefPubZdbID() {
        return antibodyDefPubZdbID;
    }

    public void setAntibodyDefPubZdbID(String antibodyDefPubZdbID) {
        this.antibodyDefPubZdbID = antibodyDefPubZdbID;
    }


    public String getRelAttribData() {
        return relAttribData;
    }

    public void setRelAttribData(String relAttribData) {
        this.relAttribData = relAttribData;
    }

    public String getPubAttribData() {
        return pubAttribData;
    }

    public void setPubAttribData(String pubAttribData) {
        this.pubAttribData = pubAttribData;
    }

    public String getNewAlias() {
        return newAlias;
    }

    public void setNewAlias(String newAlias) {
        this.newAlias = newAlias;
    }

    public Antibody getAntibody() {
        if (antibody == null) {
            antibody = new Antibody();
        }
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }


    public Map<String, String> getImmunogenOrganismList() {
        LinkedHashMap<String, String> organismList = new LinkedHashMap<String, String>();
        organismList.put("", "");
        AntibodyRepository ar = RepositoryFactory.getAntibodyRepository();
        List<Species> species = ar.getImmunogenSpeciesList();
        for (Species spec : species)
            organismList.put(spec.getCommonName(), spec.getCommonName());
        return organismList;
    }

    public Map<String, String> getAntigenOrganismList() {
        LinkedHashMap<String, String> antigenList = new LinkedHashMap<String, String>();
        antigenList.put("", "");
        AntibodyRepository ar = RepositoryFactory.getAntibodyRepository();
        List<Species> species = ar.getHostSpeciesList();
        for (Species spec : species)
            antigenList.put(spec.getCommonName(), spec.getCommonName());
        return antigenList;
    }


    public Map<String, String> getIsotypeHeavyChainList() {
        LinkedHashMap<String, String> heavyisoList = new LinkedHashMap<String, String>();
        heavyisoList.put("", "");
        Isotype.HeavyChain[] isotype = Isotype.HeavyChain.values();
        for (Isotype.HeavyChain iso : isotype)
            heavyisoList.put(iso.toString(), iso.toString());
        return heavyisoList;
    }

    public Map<String, String> getIsotypeLightChainList() {
        LinkedHashMap<String, String> lightisoList = new LinkedHashMap<String, String>();
        lightisoList.put("", "");
        Isotype.LightChain[] isotype = Isotype.LightChain.values();
        for (Isotype.LightChain iso : isotype)
            lightisoList.put(iso.toString(), iso.toString());
        return lightisoList;
    }

    public Map<String, String> getTypeList() {
        LinkedHashMap<String, String> typeList = new LinkedHashMap<String, String>();
        typeList.put(AntibodyType.UNSPECIFIED.getName(), AntibodyType.UNSPECIFIED.getName());
        typeList.put(AntibodyType.MONOCLONAL.getName(), AntibodyType.MONOCLONAL.getName());
        typeList.put(AntibodyType.POLYCLONAL.getName(), AntibodyType.POLYCLONAL.getName());
        return typeList;
    }

    public List<Publication> getPublication() {
        List<Publication> publication = new ArrayList<Publication>();


        if (antibody == null)
            return publication;

        // add alias associated publictions
        Set<MarkerAlias> mrkrAliases = antibody.getAliases();
        if (mrkrAliases != null && !mrkrAliases.isEmpty()) {
            for (MarkerAlias alias : antibody.getAliases()) {
                Set<PublicationAttribution> aliasPubs = alias.getPublications();
                if (aliasPubs != null && !aliasPubs.isEmpty()) {
                    for (PublicationAttribution pubAttr : aliasPubs)
                        publication.add(pubAttr.getPublication());
                }
            }
        }
        return publication;
    }

    public void setMostRecentPubs(List<Publication> mostRecentPubs) {
        this.mostRecentPubs = mostRecentPubs;
    }

    public Map<String, String> getDefPubList() {
        Map<String, String> entries = new LinkedHashMap<String, String>();
        entries.put("-", "Select Publication");
        entries.put("ZDB-PUB-080117-1", "Antibody Data Submissions");
        entries.put("ZDB-PUB-020723-5", "Manually Curated Data");
        if (mostRecentPubs == null || mostRecentPubs.size() == 0)
            return entries;
        entries.put("--", "--- Most Recent Pubs -----");
        for (int i = mostRecentPubs.size() - 1; i > -1; i--) {
            Publication pub = mostRecentPubs.get(i);
	    if(pub != null){
		String labelString = pub.getShortAuthorList();
		entries.put(pub.getZdbID(), labelString);
	    }
        }
        return entries;
    }

}



