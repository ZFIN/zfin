package org.zfin.uniprot.task;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Test;
import org.zfin.sequence.ForeignDB;
import org.zfin.uniprot.secondary.SecondaryTermLoadPipeline;
import org.zfin.uniprot.secondary.handlers.ActionCreator;
import org.zfin.uniprot.secondary.handlers.ActionProcessor;
import org.zfin.uniprot.secondary.handlers.AddNewDBLinksFromUniProtsActionCreator;
import org.zfin.uniprot.secondary.handlers.MarkerGoTermEvidenceActionCreator;
import org.zfin.uniprot.secondary.handlers.RemoveFromLostUniProtsActionCreator;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Handler REGISTRATION ORDER in {@link UniprotSecondaryTermLoadTask#registerPipelineHandlers()}.
 *
 * <p>Not a style check. {@link MarkerGoTermEvidenceActionCreator} derives the ec2go/interpro2go
 * annotations from the stored dblinks for its db PLUS the LOAD actions the dblink handlers put on
 * the shared action list, and it emits a DELETE for every stored annotation its derivation does
 * not reproduce. Register the *2go handler without its dblink handlers ahead of it and the
 * derived set is empty, so the load deletes the whole stream instead of refreshing it -- silently,
 * since a delete of everything looks like a normal delete action. ZFIN-10418 introduced exactly
 * that for EC by retiring the EC dblink handlers while ec2go was still enabled.
 *
 * <p>No database: registration is pure, which is the point of it being its own method.
 */
public class UniprotSecondaryTermLoadPipelineOrderTest {

    private static final String EC2GO_FLAG = "LOAD_INTERPRO2GO_EC2GO";

    @After
    public void clearFlag() {
        System.clearProperty(EC2GO_FLAG);
    }

    private List<Pair<ActionCreator, Class<? extends ActionProcessor>>> registerWithFlag(String flagValue) {
        if (flagValue == null) {
            System.clearProperty(EC2GO_FLAG);
        } else {
            System.setProperty(EC2GO_FLAG, flagValue);
        }
        UniprotSecondaryTermLoadTask task = new UniprotSecondaryTermLoadTask(
                UniprotSecondaryTermLoadTask.LoadTaskMode.REPORT,
                "input.dat", "/tmp/secondary-load-order-test.json",
                "ip2go.txt", "ec2go.txt", "up2go.txt",
                "domains.txt", "", "actions.json");
        task.setPipeline(new SecondaryTermLoadPipeline());
        task.registerPipelineHandlers();
        return task.getPipeline().getHandlerPairs();
    }

    /** Index of the first handler matching type + db, or -1. */
    private int indexOf(List<Pair<ActionCreator, Class<? extends ActionProcessor>>> handlers,
                        Class<? extends ActionCreator> type,
                        ForeignDB.AvailableName db) {
        for (int i = 0; i < handlers.size(); i++) {
            ActionCreator creator = handlers.get(i).getLeft();
            if (!type.isInstance(creator)) {
                continue;
            }
            ForeignDB.AvailableName creatorDb =
                    creator instanceof AddNewDBLinksFromUniProtsActionCreator a ? a.getDbName()
                  : creator instanceof RemoveFromLostUniProtsActionCreator r ? r.getDbName()
                  : creator instanceof MarkerGoTermEvidenceActionCreator m ? m.getDbName()
                  : null;
            if (db.equals(creatorDb)) {
                return i;
            }
        }
        return -1;
    }

    @Test
    public void ecDbLinkHandlersAreRegisteredBeforeTheEc2GoHandler() {
        var handlers = registerWithFlag("true");

        int remove = indexOf(handlers, RemoveFromLostUniProtsActionCreator.class, ForeignDB.AvailableName.EC);
        int add = indexOf(handlers, AddNewDBLinksFromUniProtsActionCreator.class, ForeignDB.AvailableName.EC);
        int ec2go = indexOf(handlers, MarkerGoTermEvidenceActionCreator.class, ForeignDB.AvailableName.EC);

        assertTrue("ec2go handler must be registered when LOAD_INTERPRO2GO_EC2GO=true", ec2go >= 0);
        assertTrue("EC RemoveFromLostUniProts handler must be registered alongside ec2go", remove >= 0);
        assertTrue("EC AddNewDBLinks handler must be registered alongside ec2go", add >= 0);
        assertTrue("EC AddNewDBLinks must precede the ec2go handler, or ec2go derives from an "
                + "empty dblink set and deletes the whole stream", add < ec2go);
        assertTrue("EC RemoveFromLostUniProts must precede the ec2go handler", remove < ec2go);
    }

    @Test
    public void interproDbLinkHandlersAreRegisteredBeforeTheInterpro2GoHandler() {
        var handlers = registerWithFlag("true");

        int add = indexOf(handlers, AddNewDBLinksFromUniProtsActionCreator.class, ForeignDB.AvailableName.INTERPRO);
        int ip2go = indexOf(handlers, MarkerGoTermEvidenceActionCreator.class, ForeignDB.AvailableName.INTERPRO);

        assertTrue("interpro2go handler must be registered", ip2go >= 0);
        assertTrue("InterPro AddNewDBLinks handler must be registered", add >= 0);
        assertTrue("InterPro AddNewDBLinks must precede the interpro2go handler", add < ip2go);
    }

    /** Unset must behave as true -- an unconfigured run keeps doing what it always did. */
    @Test
    public void flagDefaultsToOn() {
        var handlers = registerWithFlag(null);

        int add = indexOf(handlers, AddNewDBLinksFromUniProtsActionCreator.class, ForeignDB.AvailableName.EC);
        int ec2go = indexOf(handlers, MarkerGoTermEvidenceActionCreator.class, ForeignDB.AvailableName.EC);

        assertTrue("with the flag unset the ec2go handler must still be registered", ec2go >= 0);
        assertTrue("with the flag unset the EC dblink handler must still precede it", add >= 0 && add < ec2go);
    }

    /**
     * Turning the stream off must take the EC dblink handlers with it. Leaving them registered
     * would keep refreshing dblinks for a derivation that no longer runs, and would rebuild the
     * rows cutover-purge-ec-dblinks.sql just deleted.
     */
    @Test
    public void flagOffRetiresTheWholeEcStream() {
        var handlers = registerWithFlag("false");

        assertEquals("no ec2go handler when the flag is off", -1,
                indexOf(handlers, MarkerGoTermEvidenceActionCreator.class, ForeignDB.AvailableName.EC));
        assertEquals("no interpro2go handler when the flag is off", -1,
                indexOf(handlers, MarkerGoTermEvidenceActionCreator.class, ForeignDB.AvailableName.INTERPRO));
        assertEquals("no EC AddNewDBLinks handler when the flag is off", -1,
                indexOf(handlers, AddNewDBLinksFromUniProtsActionCreator.class, ForeignDB.AvailableName.EC));
        assertEquals("no EC RemoveFromLostUniProts handler when the flag is off", -1,
                indexOf(handlers, RemoveFromLostUniProtsActionCreator.class, ForeignDB.AvailableName.EC));
    }

    /** InterPro dblinks have live consumers (gene-page domains) and are never gated. */
    @Test
    public void interproDbLinkHandlersSurviveTheFlagBeingOff() {
        var handlers = registerWithFlag("false");

        assertTrue("InterPro dblink handlers are not gated by LOAD_INTERPRO2GO_EC2GO",
                indexOf(handlers, AddNewDBLinksFromUniProtsActionCreator.class, ForeignDB.AvailableName.INTERPRO) >= 0);
        assertTrue("InterPro dblink handlers are not gated by LOAD_INTERPRO2GO_EC2GO",
                indexOf(handlers, RemoveFromLostUniProtsActionCreator.class, ForeignDB.AvailableName.INTERPRO) >= 0);
    }

    /** ZFIN-10418 retired these two outright; they must not come back. */
    @Test
    public void pfamAndPrositeAreNotRegistered() {
        for (String flag : new String[]{"true", "false"}) {
            var handlers = registerWithFlag(flag);
            for (ForeignDB.AvailableName db : List.of(ForeignDB.AvailableName.PFAM, ForeignDB.AvailableName.PROSITE)) {
                assertEquals(db + " dblink add handler was retired by ZFIN-10418 (flag=" + flag + ")", -1,
                        indexOf(handlers, AddNewDBLinksFromUniProtsActionCreator.class, db));
                assertEquals(db + " dblink remove handler was retired by ZFIN-10418 (flag=" + flag + ")", -1,
                        indexOf(handlers, RemoveFromLostUniProtsActionCreator.class, db));
            }
        }
    }
}
