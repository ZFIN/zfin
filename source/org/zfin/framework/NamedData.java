package org.zfin.framework;

/**
 * An interface shared by all first class ZFIN Objects - markers, genotypes, anatomy, etc.
 * */
public interface NamedData {

    /**
     * Get the zdbID
     * @return zdbID of object
     */
    public String getZdbID();

    /**
     * Get the object name, in objects with long and short forms of a name,
     * this will be the long form.
     * @return object name
     */
    public String getName();

    /**
     * Get the object abbreviation, in objects with long and short forms of a name,
     * this will be the short form.  eg, for gene, it will actually return
     * the symbol.
     * For objects that only have a single name, the long form will be returned.
     * @return object abbreviation
     */
    public String getAbbreviation();

}
