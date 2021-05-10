package org.zfin.marker.presentation;

import org.zfin.expression.Figure;
import org.zfin.publication.Publication;
import org.zfin.sequence.DBLink;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import lombok.Getter;
import lombok.Setter;
/**
 */
@Setter
@Getter
public class ProteinDetailDomainBean {
    private String upID;
    private List<String> dbLinks;
    private ProteinDetail proDetail;

    private List<ProteinDomainRow> interProDomains;



}