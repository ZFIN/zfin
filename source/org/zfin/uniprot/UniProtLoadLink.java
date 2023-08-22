package org.zfin.uniprot;

public record UniProtLoadLink(String title, String href)  implements Comparable<UniProtLoadLink> {
    @Override
    public int compareTo(UniProtLoadLink other) {
        if (title.compareTo(other.title) != 0) {
            return title.compareTo(other.title);
        } else {
            return href.compareTo(other.href);
        }
    }
}