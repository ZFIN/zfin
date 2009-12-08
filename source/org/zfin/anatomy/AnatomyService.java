package org.zfin.anatomy;

import org.zfin.anatomy.presentation.RelationshipPresentation;
import org.zfin.anatomy.presentation.RelationshipSorting;
import org.zfin.anatomy.presentation.AnatomyPresentation;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class AnatomyService {

    private static AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();

    public static List<RelationshipPresentation> getRelations(AnatomyItem term) {
        Set<String> types = new HashSet<String>();
        List<AnatomyRelationship> relatedItems = term.getRelatedItems();
        if (relatedItems == null){
            relatedItems = anatomyRepository.getAnatomyRelationships(term);
            term.setRelatedItems(relatedItems);
        }
        if (relatedItems != null) {
            for (AnatomyRelationship rel : relatedItems) {
                types.add(rel.getRelationship());
            }
        }
        List<String> uniqueTypes = new ArrayList<String>(types);
        Collections.sort(uniqueTypes, new RelationshipSorting());
        return AnatomyPresentation.createRelationshipPresentation(uniqueTypes, term);

    }

}
