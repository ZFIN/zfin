package org.zfin.uniprot;

public record UniProtLoadTag(String name, String value)  implements Comparable<UniProtLoadTag> {
    @Override
    public int compareTo(UniProtLoadTag other) {
        if (name.compareTo(other.name) != 0) {
            return name.compareTo(other.name);
        } else {
            return value.compareTo(other.value);
        }
    }
}