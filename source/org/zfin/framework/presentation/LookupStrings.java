package org.zfin.framework.presentation;

import org.springframework.ui.Model;

/**
 * *  Class LookupStrings.
 */
public class LookupStrings {

    public final static String DYNAMIC_TITLE = "dynamicTitle";
    public final static String FORM_BEAN = "formBean";
    public final static String IS_OWNER = "isOwner";
    public final static String ERRORS = "errors";

    // lookups
    public final static String ZDB_ID = "zdbID";
    public final static String ACCESSION = "accession";
    public final static String REF_DB = "refDB";
    public final static String BLAST_DB = "blastDB";


    public static final String RECORD_NOT_FOUND_PAGE = "record-not-found.page";
    public static final String ERROR_PAGE = "error-page";

    // suffix
    public final static String PAGE_SUFFIX = ".page";
    public final static String INSERT_SUFFIX = ".insert";

    // form processing
    public static final String SELECTED_TAB = "selectedTab";
    public static final String EXCEPTION = "exception";

    public static String idNotFound(Model model, String fishID) {
        model.addAttribute(LookupStrings.ZDB_ID, fishID);
        return LookupStrings.RECORD_NOT_FOUND_PAGE;
    }
}

