package org.zfin.zirc.api;

import org.junit.Test;
import org.zfin.zirc.api.uischema.Control;
import org.zfin.zirc.api.uischema.Group;
import org.zfin.zirc.api.uischema.Rule;
import org.zfin.zirc.api.uischema.UiSchemaElement;
import org.zfin.zirc.api.uischema.VerticalLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * The mutagenesis-protocol sequence fields, moved here from the lesion form.
 *
 * <p>Each reveal rule matches a {@code mutagenesisProtocol} value exactly, so
 * the risk worth guarding is the rule and the dropdown drifting apart: rename
 * an option and its box silently stops appearing, with nothing failing.
 */
public class ZircMutationFormSchemaTest {

    /** The Controls the protocol reveals, by scope. */
    private static final String CRISPR_SCOPE = "#/properties/crisprSequence";
    private static final String TALEN_1_SCOPE = "#/properties/talenSequence1";
    private static final String TALEN_2_SCOPE = "#/properties/talenSequence2";

    private static List<Control> controls() {
        List<Control> out = new ArrayList<>();
        collect(ZircMutationFormSchema.uiSchema(), out);
        return out;
    }

    private static void collect(UiSchemaElement element, List<Control> out) {
        if (element instanceof Control c) {
            out.add(c);
        } else if (element instanceof Group g) {
            g.elements().forEach(e -> collect(e, out));
        } else if (element instanceof VerticalLayout v) {
            v.elements().forEach(e -> collect(e, out));
        }
    }

    private static Control controlFor(String scope) {
        return controls().stream()
                .filter(c -> scope.equals(c.scope()))
                .findFirst()
                .orElse(null);
    }

    /** The value a Control's SHOW rule tests mutagenesisProtocol against. */
    private static List<?> revealedBy(Control control) {
        assertNotNull("no rule on " + control.scope(), control.rule());
        assertEquals(Rule.Effect.SHOW, control.rule().effect());
        Rule.RuleCondition condition = (Rule.RuleCondition) control.rule().condition();
        assertEquals("#/properties/mutagenesisProtocol", condition.scope());
        Object values = condition.schema().get("enum");
        assertTrue("expected an enum match, got " + condition.schema(),
                values instanceof List);
        return (List<?>) values;
    }

    /**
     * The tokens the rules key off must be options the dropdown actually
     * offers, or the box can never appear.
     */
    @Test
    public void revealTokensAreOfferedByTheProtocolDropdown() {
        Control protocol = controlFor("#/properties/mutagenesisProtocol");
        assertNotNull("no mutagenesisProtocol Control", protocol);
        List<String> offered = protocol.options().standardValues();
        assertTrue("dropdown does not offer " + ZircMutationFormSchema.PROTOCOL_CRISPR
                        + "; offered=" + offered,
                offered.contains(ZircMutationFormSchema.PROTOCOL_CRISPR));
        assertTrue("dropdown does not offer " + ZircMutationFormSchema.PROTOCOL_TALEN
                        + "; offered=" + offered,
                offered.contains(ZircMutationFormSchema.PROTOCOL_TALEN));
    }

    @Test
    public void crisprSequenceIsRevealedByTheCrisprProtocol() {
        assertEquals(List.of(ZircMutationFormSchema.PROTOCOL_CRISPR),
                revealedBy(controlFor(CRISPR_SCOPE)));
    }

    /** TALENs act as a pair, so both boxes ride the same protocol. */
    @Test
    public void bothTalenSequencesAreRevealedByTheTalenProtocol() {
        assertEquals(List.of(ZircMutationFormSchema.PROTOCOL_TALEN),
                revealedBy(controlFor(TALEN_1_SCOPE)));
        assertEquals(List.of(ZircMutationFormSchema.PROTOCOL_TALEN),
                revealedBy(controlFor(TALEN_2_SCOPE)));
    }

    /**
     * Each sequence box carries the nucleotide widget with an explicit
     * alphabet — the treatment these fields had before the move, and what
     * keeps the widget and the server-side coercer on one value.
     */
    @Test
    public void sequenceControlsDeclareTheNucleotideWidgetAndAlphabet() {
        for (String scope : List.of(CRISPR_SCOPE, TALEN_1_SCOPE, TALEN_2_SCOPE)) {
            Control c = controlFor(scope);
            assertNotNull("missing Control " + scope, c);
            assertEquals(scope + " widget", "nucleotideSequence", c.options().widget());
            assertEquals(scope + " alphabet", "ACGT", c.options().alphabet());
            assertEquals(scope + " should be multi-line", Boolean.TRUE, c.options().multi());
        }
    }

    /** All three are patchable, or the form could not save them. */
    @Test
    public void sequenceFieldsArePatchable() {
        for (String path : List.of("/crisprSequence", "/talenSequence1", "/talenSequence2")) {
            assertTrue("FIELDS is missing " + path,
                    ZircMutationFormSchema.FIELDS.containsKey(path));
        }
    }

    /**
     * They are optional: a mutation made by ENU has no sequence to give, and
     * a required box hidden behind another protocol could never be satisfied.
     */
    @Test
    public void sequenceFieldsAreNotRequired() {
        Map<String, ?> ignored = Map.of();
        List<String> required =
                ((org.zfin.zirc.api.jsonschema.ObjectSchema) ZircMutationFormSchema.schema())
                        .required();
        for (String name : List.of("crisprSequence", "talenSequence1", "talenSequence2")) {
            assertTrue(name + " must not be required; required=" + required,
                    required == null || !required.contains(name));
        }
    }

    /**
     * They render inside the Mutagenesis group, next to the protocol that
     * reveals them, and not in some other section.
     */
    @Test
    public void sequenceControlsLiveInTheMutagenesisGroup() {
        VerticalLayout root = (VerticalLayout) ZircMutationFormSchema.uiSchema();
        Group mutagenesis = root.elements().stream()
                .filter(Group.class::isInstance)
                .map(Group.class::cast)
                .filter(g -> "Mutagenesis".equals(g.label()))
                .findFirst()
                .orElse(null);
        assertNotNull("no Mutagenesis group", mutagenesis);
        List<Control> inGroup = new ArrayList<>();
        collect(mutagenesis, inGroup);
        List<String> scopes = inGroup.stream().map(Control::scope).toList();
        for (String scope : List.of(CRISPR_SCOPE, TALEN_1_SCOPE, TALEN_2_SCOPE)) {
            assertTrue(scope + " should sit in the Mutagenesis group; found " + scopes,
                    scopes.contains(scope));
        }
    }
}
