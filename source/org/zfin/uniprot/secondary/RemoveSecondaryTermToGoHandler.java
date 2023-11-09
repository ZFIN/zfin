package org.zfin.uniprot.secondary;

import lombok.extern.log4j.Log4j2;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.List;
import java.util.Map;

@Log4j2
public class RemoveSecondaryTermToGoHandler implements SecondaryLoadHandler {

    private final ForeignDB.AvailableName dbName;
    private final List<SecondaryTerm2GoTerm> translationRecords;

    public RemoveSecondaryTermToGoHandler(ForeignDB.AvailableName dbName, List<SecondaryTerm2GoTerm> translationRecords) {
        this.dbName = dbName;
        this.translationRecords = translationRecords;
    }

    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, List<SecondaryTermLoadAction> actions, SecondaryLoadContext context) {
        List<SecondaryTermLoadAction> deletes = actions.stream()
                .filter(action -> dbName.equals(action.getDbName())
                        && action.getType().equals(SecondaryTermLoadAction.Type.DELETE)
                        && action.getSubType().equals(SecondaryTermLoadAction.SubType.DB_LINK))
                .toList();

        log.debug("Joining " + deletes.size()  + " SecondaryLoadAction against " + translationRecords.size() + " " + dbName + " translation records ");

        log.debug("DELETING marker_go_term_evidence");
        //join the load actions to the interpro translation records
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
                    .subType(SecondaryTermLoadAction.SubType.MARKER_GO_TERM_EVIDENCE)
                    .geneZdbID(action.getGeneZdbID())
                    .goID(item2go.goID())
                    .goTermZdbID(item2go.termZdbID())
                    .build();
            actions.add(newAction);
        }

    }
}
