package org.zfin.sequence.gff;

public enum AssemblyEnum  {

    GRCZ12TU("GRCz12tu"),
    GRCZ11("GRCz11"),
    GRCZ10("GRCz10"),
    ZV9("Zv9")
    ;

    private String name;

    AssemblyEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

