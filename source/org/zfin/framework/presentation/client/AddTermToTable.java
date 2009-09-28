package org.zfin.framework.presentation.client;

/**
 */
public class AddTermToTable implements SubmitAction{

    private LookupFieldValidator lookupFieldValidator ;

    public AddTermToTable(LookupFieldValidator lookupFieldValidator){
        this.lookupFieldValidator = lookupFieldValidator ;
    }

    public void doSubmit(String value) {
        lookupFieldValidator.validateLookup() ;
    }
}
