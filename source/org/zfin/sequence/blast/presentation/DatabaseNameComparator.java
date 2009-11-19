package org.zfin.sequence.blast.presentation;

import java.util.Comparator;

public class DatabaseNameComparator implements Comparator<DatabasePresentationBean> {
    public int compare(DatabasePresentationBean o1, DatabasePresentationBean o2) {
        return (o1.getDatabase().getName().compareTo(o2.getDatabase().getName())) ;
    }
}
