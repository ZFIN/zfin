package org.zfin.infrastructure;

import org.zfin.publication.Publication;

import java.util.Set;


public interface EntityAttribution {
    Set<PublicationAttribution> getPublications();
    Publication getSinglePublication();
    int getPublicationCount();
}
