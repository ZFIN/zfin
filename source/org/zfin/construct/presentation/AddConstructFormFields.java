package org.zfin.construct.presentation;

import lombok.Data;
import org.zfin.construct.name.ConstructName;

import jakarta.servlet.http.HttpServletRequest;

@Data
public class AddConstructFormFields {

    private ConstructName constructNameObject;
    private String constructName;
    private String pubZdbID;
    private String constructAlias;
    private String constructSequence;
    private String constructComments;
    private String constructCuratorNote;
    private String constructStoredName;
    private String constructType;
    private String constructPrefix;

    public static AddConstructFormFields fromRequest(HttpServletRequest request) {
        AddConstructFormFields formFields = new AddConstructFormFields();
        formFields.setConstructName(request.getParameter("constructName"));
        formFields.setPubZdbID(request.getParameter("constructPublicationZdbID"));
        formFields.setConstructAlias(request.getParameter("constructAlias"));
        formFields.setConstructSequence(request.getParameter("constructSequence"));
        formFields.setConstructComments(request.getParameter("constructComments"));
        formFields.setConstructCuratorNote(request.getParameter("constructCuratorNote"));
        formFields.setConstructStoredName(request.getParameter("constructStoredName"));
        formFields.setConstructType(request.getParameter("chosenType"));
        formFields.setConstructPrefix(request.getParameter("prefix"));

        return formFields;
    }

}
