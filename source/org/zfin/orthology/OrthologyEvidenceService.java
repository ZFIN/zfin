package org.zfin.orthology;

import java.util.*;

/**
 * service class that deals with orthology evidence handling.
 */
public class OrthologyEvidenceService {

    /**
     * Create a set of fast-search orthologue evidence objects.
     * 1) Each evidence code for each organism is its own OrthologueEvidenceFastSearch object,
     * unless:
     * 2) If two different organism have the same evidence code and the same publication
     * they are joined in a single OrthologueEvidenceFastSearch object
     *
     * @param orthologs set of Orthology objects
     * @return a set of fast search objects.
     */
    public static Set<OrthologyEvidenceFastSearch> getOrthoEvidenceFastSearches(Set<Ortholog> orthologs) {
        // first get a list of all evidence codes being used:
        Set<OrthologyEvidenceFastSearch> fastSearchEvidences = new HashSet<>();
        for (Ortholog ortho : orthologs) {
            org.zfin.Species organism = ortho.getNcbiOtherSpeciesGene().getOrganism();
            for (OrthologEvidence evidence : ortho.getEvidenceSet()) {
                OrthologyEvidenceFastSearch fastSearch = new OrthologyEvidenceFastSearch();
                /// fastSearch.setCode(evidence);
                fastSearch.setMarker(ortho.getZebrafishGene());
                fastSearch.setPublication(evidence.getPublication());
                fastSearch.setOrganism(organism.toString());
                if (!fastSearchEvidences.contains(fastSearch))
                    fastSearchEvidences.add(fastSearch);
                else {
                    for (OrthologyEvidenceFastSearch ev : fastSearchEvidences) {
                        if (ev.equals(fastSearch)) {
                            // create colon-delimited
                            ev.setOrganism(ev.getOrganism() + ":" + fastSearch.getOrganism());
                        }
                    }
                }
            }
        }
        return fastSearchEvidences;
    }

    /**
     * Creates a new orthology list sorted by evidence / publication.
     *
     * @param list Orthology
     * @return list of Orthology
     */
    public static List<Orthology> getEvidenceCenteredList(List<Orthology> list) {
        List<Orthology> returnList = new ArrayList<>(list.size());
        returnList.addAll(list);
        Collections.sort(returnList, new Comparator<Orthology>() {
            @Override
            public int compare(Orthology o1, Orthology o2) {
                if (o1.getEvidenceCode().equals(o2.getEvidenceCode()))
                    return o1.getPublication().getShortAuthorList().compareTo(o2.getPublication().getShortAuthorList());
                else
                    return o1.getEvidenceCode().getOrder().compareTo(o2.getEvidenceCode().getOrder());
            }
        });
        return returnList;
    }


}
