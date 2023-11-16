package org.zfin.uniprot.secondary.handlers;

import lombok.extern.log4j.Log4j2;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.adapter.RichSequenceAdapter;
import org.zfin.uniprot.secondary.SecondaryLoadContext;
import org.zfin.uniprot.secondary.SecondaryTerm2GoTerm;
import org.zfin.uniprot.secondary.SecondaryTermLoadAction;

import java.util.List;
import java.util.Map;

/**
 * Remove from our marker_go_term_evidence table if the new uniprot release no longer contains it.
 * This is related to AddNewSecondaryTermToGoHandler which handles inserts
 */
@Log4j2
public class RemoveSecondaryTermToGoActionCreator implements ActionCreator {
    @Override
    public SecondaryTermLoadAction.SubType isSubTypeHandlerFor() {
        return SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE;
    }
    private final ForeignDB.AvailableName dbName;

    protected final List<SecondaryTerm2GoTerm> translationRecords;

    public RemoveSecondaryTermToGoActionCreator(ForeignDB.AvailableName dbName, List<SecondaryTerm2GoTerm> translationRecords) {
        this.dbName = dbName;
        this.translationRecords = translationRecords;
    }

    @Override
    public List<SecondaryTermLoadAction> createActions(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        List<SecondaryTermLoadAction> deletes = actions.stream()
                .filter(action -> dbName.equals(action.getDbName())
                        && action.getType().equals(SecondaryTermLoadAction.Type.DELETE)
                        && action.getSubType().equals(SecondaryTermLoadAction.SubType.DB_LINK))
                .toList();

        log.debug("Joining " + deletes.size()  + " SecondaryLoadAction against " + translationRecords.size() + " " + dbName + " translation records ");

        //join the load actions to the interpro translation records
        List<SecondaryTermLoadAction> newDeleteActions = new java.util.ArrayList<>();
        List<Tuple2<SecondaryTermLoadAction, SecondaryTerm2GoTerm>> joined = Seq.seq(deletes)
                .innerJoin(translationRecords,
                        (action, item2go) -> action.getAccession().equals(item2go.dbAccession()))
                .toList();
        for(var joinedRecord : joined) {
            SecondaryTermLoadAction action = joinedRecord.v1();
            SecondaryTerm2GoTerm item2go = joinedRecord.v2();
            SecondaryTermLoadAction newAction = SecondaryTermLoadAction.builder()
                    .accession(action.getAccession())
                    .dbName(dbName)
                    .type(SecondaryTermLoadAction.Type.DELETE)
                    .subType(isSubTypeHandlerFor())
                    .geneZdbID(action.getGeneZdbID())
                    .goID(item2go.goID())
                    .goTermZdbID(item2go.termZdbID())
                    .build();
            newDeleteActions.add(newAction);
        }
        return newDeleteActions;
    }

}
