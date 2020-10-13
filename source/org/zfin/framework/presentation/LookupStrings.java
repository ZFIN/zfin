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

    // GWT lookup constants
    public static final String ACTION_TERM_SEARCH = "TERM_SEARCH";
    public static final String ACTION_ANATOMY_SEARCH = "ANATOMY_SEARCH";
    public static final String ACTION_GENEDOM_AND_EFG_SEARCH = "GENEDOM_AND_EFG_SEARCH";
    public static final String ACTION_MARKER_ATTRIBUTE = "MARKER_ATTRIBUTE";
    public static final String ACTION_FEATURE_ATTRIBUTE = "FEATURE_ATTRIBUTE";
    public static final String GENEDOM_AND_EFG = "GENEDOM_AND_EFG_LOOKUP";
    public static final String GENEDOM = "GENEDOM";
    public static final String MARKER_LOOKUP = "MARKER_LOOKUP";
    public static final String ANTIBODY_LOOKUP = "ANTIBODY_LOOKUP";
    public static final String TYPE_SUPPLIER = "SUPPLIER";
    public static final String FEATURE_LOOKUP = "FEATURE_LOOKUP";
    public static final String GDAG_TERM_LOOKUP = "GDAG_TERM_LOOKUP";
    public static final String MARKER_LOOKUP_AND_TYPE = "MARKER_LOOKUP_AND_TYPE";
    public static final String CONSTRUCT_LOOKUP = "CONSTRUCT_LOOKUP";
    public static final String JSREF_DIV_NAME = "divName";
    public static final String JSREF_INPUT_NAME = "inputName";
    public static final String JSREF_TYPE = "type";
    public static final String JSREF_SHOWERROR = "showError";
    public static final String JSREF_BUTTONTEXT = "buttonText";
    public static final String JSREF_WILDCARD = "wildcard";
    public static final String JSREF_WIDTH = "width";
    public static final String JSREF_LIMIT = "limit";
    public static final String JSREF_ACTION = "action";
    public static final String JSREF_ONCLICK = "onclick";
    public static final String JSREF_OID = "OID";
    public static final String JSREF_ONTOLOGY_NAME = "ontologyName";
    public static final String JSREF_USE_ID_AS_TERM = "useIdAsTerm";
    public static final String JSREF_TERMS_WITH_DATA_ONLY = "termsWithDataOnly";
    public static final String JSREF_ANATOMY_TERMS_ONLY = "anatomyTermsOnly";
    public static final String JSREF_HIDDEN_NAME = "hiddenNames";
    public static final String JSREF_HIDDEN_IDS = "hiddenIds";
    public static final String JSREF_PREVIOUS_TABLE_VALUES = "previousTableValues";
    public static final String JSREF_IMAGE_URL = "imageURL";
    public static final String JSREF_USE_TERM_TABLE = "useTermTable";
    public static final String JSREF_INPUT_DIV = "inputDiv";
    public static final String JSREF_TERM_LIST_DIV = "termListDiv";

    public static String idNotFound(Model model, String fishID) {
        model.addAttribute(LookupStrings.ZDB_ID, fishID);
        return LookupStrings.RECORD_NOT_FOUND_PAGE;
    }
}

