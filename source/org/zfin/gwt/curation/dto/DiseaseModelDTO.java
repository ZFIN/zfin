package org.zfin.gwt.curation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.dto.TermDTO;

/**
 * Created by cmpich on 3/31/15.
 */
public class DiseaseModelDTO implements IsSerializable {

    private TermDTO term;
    private GenotypeDTO genotype;
    private EnvironmentDTO environment;


    public EnvironmentDTO getEnvironment() {
        return environment;
    }

    public void setEnvironment(EnvironmentDTO environment) {
        this.environment = environment;
    }

    public GenotypeDTO getGenotype() {
        return genotype;
    }

    public void setGenotype(GenotypeDTO genotype) {
        this.genotype = genotype;
    }

    public TermDTO getTerm() {
        return term;
    }

    public void setTerm(TermDTO term) {
        this.term = term;
    }
}
