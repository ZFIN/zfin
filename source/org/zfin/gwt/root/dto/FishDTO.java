package org.zfin.gwt.root.dto;

import java.util.List;

/**
 * GWT Data Transfer Object corresponding to {@link org.zfin.mutant.Genotype}
 */
public class FishDTO extends RelatedEntityDTO implements Comparable {

    private String zdbID;
    private String name;
    private String handle;
    private GenotypeDTO genotypeDTO;
    private List<RelatedEntityDTO> strList;
    private long order;
    private String nameOrder;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RelatedEntityDTO> getStrList() {
        return strList;
    }

    public GenotypeDTO getGenotypeDTO() {
        return genotypeDTO;
    }

    public void setGenotypeDTO(GenotypeDTO genotypeDTO) {
        this.genotypeDTO = genotypeDTO;
    }

    public void setStrList(List<RelatedEntityDTO> strList) {
        this.strList = strList;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public String getNameOrder() {
        return nameOrder;
    }

    public void setNameOrder(String nameOrder) {
        this.nameOrder = nameOrder;
    }

    public String getStrDisplayName() {
        if (strList == null)
            return "";
        String name = "";
        for (RelatedEntityDTO dto : strList)
            name += dto.getName() + ", ";
        name = name.substring(0, name.length() - 2);
        return name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FishDTO fishDTO = (FishDTO) o;
        if (!genotypeDTO.equals(fishDTO.genotypeDTO)) return false;
        if (strList != null ? !strList.equals(fishDTO.strList) : fishDTO.strList != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + genotypeDTO.hashCode();
        result = 31 * result + (strList != null ? strList.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof FishDTO))
            return -1;
        FishDTO dto = (FishDTO) o;

        int compareOrder = Long.compare(order, dto.getOrder());
        if (compareOrder != 0) {
            return compareOrder;
        }
        return nameOrder.compareTo(dto.getNameOrder());
    }

    public int compareToWildtypeFirst(FishDTO fish) {
        if (genotypeDTO.isWildtype() && !fish.getGenotypeDTO().isWildtype())
            return -1;
        if (!genotypeDTO.isWildtype() && fish.getGenotypeDTO().isWildtype())
            return 1;
        return fish.getNameOrder().compareTo(fish.getNameOrder());

    }

    public boolean isWildtype() {
        return genotypeDTO.isWildtype() && (strList == null || strList.size() == 0);
    }
}
