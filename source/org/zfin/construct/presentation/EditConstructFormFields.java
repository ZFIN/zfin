package org.zfin.construct.presentation;

import lombok.Data;
import org.zfin.construct.name.ConstructName;

import java.util.List;

@Data
public class EditConstructFormFields {
    private ConstructName constructName;
    private List<MarkerNameAndZdbId> synonyms;
    private List<MarkerNameAndZdbId> sequences;
    private List<MarkerNameAndZdbId> notes;
    private String publicNote;
    private String publicationZdbID;
}
