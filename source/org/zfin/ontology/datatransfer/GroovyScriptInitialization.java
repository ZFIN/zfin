package org.zfin.ontology.datatransfer;

/**
 * Created by cmpich on 4/4/14.
 */
public class GroovyScriptInitialization extends AbstractScriptWrapper {


    public static void main(String[] arguments) {
        String propertyFile = arguments[0];
        System.out.print("Property File: " + propertyFile);
        GroovyScriptInitialization script = new GroovyScriptInitialization();
        script.initAll(propertyFile);
    }
}
