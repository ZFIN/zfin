package org.zfin.people;

import org.apache.commons.lang.ObjectUtils;
import org.zfin.mutant.Genotype;

import java.io.Serializable;

/**
 * Main domain object for lab and company info
 */
public class GenotypeSupplier extends ObjectSupplier implements Serializable, Comparable<GenotypeSupplier> {

    private Genotype genotype;

    public String getOrderURL() {
        if (organization.getOrganizationOrderURL() != null && organization.getOrganizationOrderURL().getUrlPrefix() != null && accNum != null)
          return organization.getOrganizationOrderURL().getUrlPrefix() + accNum;
        return null;
    }

    public int hashCode() {
        int num = 39;
        if (genotype != null)
            num += genotype.hashCode();
        if (genotype != null)
            num += genotype.hashCode();
        return num;
    }

    /**
     * This method assumes that dataZdbID and supplierZdbID are not null.
     * Otherwise this method throws an exception.
     *
     * @param o Object
     * @return boolean
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof GenotypeSupplier))
            return false;
        GenotypeSupplier supplier = (GenotypeSupplier) o;

        if (genotype == null)
            throw new RuntimeException("Genotype is null but should not!");
        if (organization == null)
            throw new RuntimeException("organization is null but should not!");

        return genotype.equals(supplier.getGenotype()) &&
                ObjectUtils.equals(organization, supplier.getOrganization());
    }

    public int compareTo(GenotypeSupplier anotherSupplier) {
        if (anotherSupplier == null || anotherSupplier.getOrganization() == null || anotherSupplier.getOrganization().getName() == null)
            return -1;
        if (getOrganization() == null || getOrganization().getName() == null)
            return +1;
        return getOrganization().getName().compareToIgnoreCase(anotherSupplier.getOrganization().getName());
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
        dataZdbID = genotype.getZdbID();
    }

    public boolean isZirc() {
		if (this.getOrganization().getZdbID().equals("ZDB-LAB-991005-53"))
		  return true;
		else
		  return false;
	}

	public boolean isRiken() {
		if (this.getOrganization().getZdbID().equals("ZDB-LAB-070718-1"))
		  return true;
		else
		  return false;
	}
}
