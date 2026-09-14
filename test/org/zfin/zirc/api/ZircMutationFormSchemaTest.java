package org.zfin.zirc.api;

import org.junit.Test;
import org.zfin.gwt.root.dto.Mutagen;
import org.zfin.zirc.api.uischema.Control;
import org.zfin.zirc.api.uischema.Group;
import org.zfin.zirc.api.uischema.Rule;
import org.zfin.zirc.api.uischema.UiSchemaElement;
import org.zfin.zirc.api.uischema.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * The mutagenesis-protocol sequence fields, moved here from the lesion form.
 *
 * <p>Each reveal rule matches {@code mutagenesisProtocol} against an exact set
 * of values, so the risk worth guarding is the rule and the dropdown drifting
 * apart: rename an option and its box silently stops appearing, with nothing
 * failing.
 *
 * <p>The expected tokens are spelled out from {@link Mutagen} here rather than
 * read back off {@code ZircMutationFormSchema}'s own constants. A test that
 * asserts a constant equals itself cannot catch that constant being wrong --
 * this one states independently which four protocols are supposed to ask for a
 * reagent sequence, so narrowing the production list fails here.
 */
public class ZircMutationFormSchemaTest {

    /** The Controls the protocol reveals, by scope. */
    private static final String CRISPR_SCOPE = "#/properties/crisprSequence";
    private static final String TALEN_1_SCOPE = "#/properties/talenSequence1";
    private static final String TALEN_2_SCOPE = "#/properties/talenSequence2";

    /**
     * The protocols that use each reagent. The DNA_AND_* variants belong:
     * a DNA-and-CRISPR mutagenesis still used a guide, and a rule matching
     * only the bare token would hide the box for half the submissions that
     * need it.
     */
    private static final List<String> CRISPR_PROTOCOLS =
            List.of(Mutagen.CRISPR.toString(), Mutagen.DNA_AND_CRISPR.toString());
    private static final List<String> TALEN_PROTOCOLS =
            List.of(Mutagen.TALEN.toString(), Mutagen.DNA_AND_TALEN.toString());

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
        for (String token : CRISPR_PROTOCOLS) {
            assertTrue("dropdown does not offer " + token + "; offered=" + offered,
                    offered.contains(token));
        }
        for (String token : TALEN_PROTOCOLS) {
            assertTrue("dropdown does not offer " + token + "; offered=" + offered,
                    offered.contains(token));
        }
    }

    @Test
    public void crisprSequenceIsRevealedByEveryCrisprProtocol() {
        assertEquals(CRISPR_PROTOCOLS, revealedBy(controlFor(CRISPR_SCOPE)));
    }

    /** TALENs act as a pair, so both boxes ride the same protocols. */
    @Test
    public void bothTalenSequencesAreRevealedByEveryTalenProtocol() {
        assertEquals(TALEN_PROTOCOLS, revealedBy(controlFor(TALEN_1_SCOPE)));
        assertEquals(TALEN_PROTOCOLS, revealedBy(controlFor(TALEN_2_SCOPE)));
    }

    /**
     * The other seven protocols use neither reagent, so neither box should
     * appear under them -- the complement of the two tests above, which on
     * their own would pass just as well if a rule listed every protocol.
     */
    @Test
    public void noSequenceIsRevealedByAProtocolThatUsesNeitherReagent() {
        for (Mutagen m : ZircMutationFormSchema.MUTAGENESIS_PROTOCOL_ORDER) {
            String token = m.toString();
            if (CRISPR_PROTOCOLS.contains(token) || TALEN_PROTOCOLS.contains(token)) {
                continue;
            }
            for (String scope : List.of(CRISPR_SCOPE, TALEN_1_SCOPE, TALEN_2_SCOPE)) {
                assertTrue(token + " must not reveal " + scope,
                        !revealedBy(controlFor(scope)).contains(token));
            }
        }
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
