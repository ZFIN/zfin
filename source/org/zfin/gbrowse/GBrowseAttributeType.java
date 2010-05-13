package org.zfin.gbrowse;

import java.io.Serializable;

/**
 * This class is a direct mapping of a table in the Bio::Seqfeature::Store schema
 * <p/>
 * It acts as a controlled vocabulary - the one value that we use within this
 * vocabulary is zdb_id.
 * <p/>
 * When we build gff3 files, we populate the Alias field, but that isn't
 * accessed from the java side.
 */
public class GBrowseAttributeType implements Serializable {
    public static final String ZDBID = "zdb_id";

    private Integer id;
    private String tag;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
