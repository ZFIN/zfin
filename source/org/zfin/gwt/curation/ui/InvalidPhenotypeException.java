package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.dto.PhenotypeStatementDTO;

/**
 * Exception for invalid ontology combinations.
 */
public class InvalidPhenotypeException extends Exception implements IsSerializable{

    public InvalidPhenotypeException() {
    }

    public InvalidPhenotypeException(PhenotypeStatementDTO structure) {
        super("Invalid Structure [" + structure.getDisplayName() + "]: The combination of ontologies is prohibited. ");
    }
}