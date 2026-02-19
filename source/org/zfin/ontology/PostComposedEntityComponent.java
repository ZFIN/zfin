package org.zfin.ontology;

import jakarta.persistence.Embeddable;

/**
 * Embeddable version of PostComposedEntity for use as a component
 * in entities like PhenotypeStatement and PhenotypeStructure.
 *
 * Inherits superterm/subterm ManyToOne mappings from the
 * MappedSuperclass PostComposedEntity.
 */
@Embeddable
public class PostComposedEntityComponent extends PostComposedEntity {
}
