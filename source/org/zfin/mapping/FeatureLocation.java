package org.zfin.mapping;

import lombok.Getter;
import lombok.Setter;
import org.zfin.feature.Feature;

/**
 * Feature Location .
 */
@Setter
@Getter
public class FeatureLocation extends GenericChromosomalLocation {

    private Feature feature;

}
