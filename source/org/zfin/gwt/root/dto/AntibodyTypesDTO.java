package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

/**
 */
public class AntibodyTypesDTO implements IsSerializable {
    private List<String> hostOrganisms ;
    private List<String> immunogenOrganisms ;
    private List<String> heavyChains ;
    private List<String> lightChains ;
    private List<String> types;

    public List<String> getHostOrganisms() {
        return hostOrganisms;
    }

    public void setHostOrganisms(List<String> hostOrganisms) {
        this.hostOrganisms = hostOrganisms;
    }

    public List<String> getImmunogenOrganisms() {
        return immunogenOrganisms;
    }

    public void setImmunogenOrganisms(List<String> immunogenOrganisms) {
        this.immunogenOrganisms = immunogenOrganisms;
    }

    public List<String> getHeavyChains() {
        return heavyChains;
    }

    public void setHeavyChains(List<String> heavyChains) {
        this.heavyChains = heavyChains;
    }

    public List<String> getLightChains() {
        return lightChains;
    }

    public void setLightChains(List<String> lightChains) {
        this.lightChains = lightChains;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }
}
