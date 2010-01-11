package org.zfin.gwt.lookup.ui;

import com.google.gwt.user.client.Window;

/**
 */
public class FeatureAttributeSubmitAction extends AbstractAttributeSubmitAction{

    public FeatureAttributeSubmitAction(String OID){
        super(OID) ;
    }

    public void doSubmit(String value) {

        if(OID == null){
            Window.alert("Can not proceed OID is null");
            return ;
        }

        if(value!=null){
            setCookieInPage("attribution_update","update","pubcur_c_");
            setCookieInPage("anchor","attrib","pubcur_c_");
            Window.open("?MIval=aa-curation.apg&OID="+OID+"&pubcur_G_fish_attrib="+value+"#attrib",
                    "_self", "");
        }
    }
}
