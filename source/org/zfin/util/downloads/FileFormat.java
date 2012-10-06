package org.zfin.util.downloads;

/**
 * Enumeration of supported file formats.
 */
public enum FileFormat {

    CSV, GFF3, TSV, OTHER;

    public static FileFormat getTypeByName(String name) {
        if (name == null)
            return null;
        if (name.equalsIgnoreCase("txt"))
            name = "tsv";
        for (FileFormat type : values())
            if (type.toString().equalsIgnoreCase(name.toUpperCase()))
                return type;
        return null;
    }

    public String getFormattedFileName(String filePrefix) {
        return filePrefix + "." + name().toLowerCase();
    }

}
