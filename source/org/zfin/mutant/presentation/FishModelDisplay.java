package org.zfin.mutant.presentation;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonView;

import org.zfin.framework.api.View;
import org.apache.commons.collections4.CollectionUtils;

import org.zfin.mutant.FishExperiment;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;

/**
 * Disease model which groups by Publications
 */
@Setter
@Getter
public class FishModelDisplay implements Comparable<FishModelDisplay> {

    @JsonView(View.API.class)
    private Set<Publication> publications;
    @JsonView(View.API.class)
    private FishExperiment fishModel;


    public FishModelDisplay(FishExperiment fishModel) {
        this.fishModel = fishModel;
    }

    public Publication getPublication() {
        if (CollectionUtils.isEmpty(publications))
            return null;
        return publications.iterator().next();
    }

    public void addPublication(Publication publication) {
        if (publications == null)
            publications = new HashSet<>();
        publications.add(publication);
    }

    @Override
    public int compareTo(FishModelDisplay o) {
        if (o == null || o.getFishModel() == null) {
            return 1;
        }
        return fishModel.compareTo(o.getFishModel());
    }
}
