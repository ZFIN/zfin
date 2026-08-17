import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import * as fs from 'node:fs';
import * as path from 'node:path';
import { NOT_APPLICABLE } from '@jsonforms/core';
import type { JsonFormsRendererRegistryEntry } from '@jsonforms/core';
import { fieldRenderers } from '../schemaForm/fieldRenderers';
import { aggregateRenderers } from '../schemaForm/aggregateRenderers';
import { submissionRenderers } from '../schemaForm/SchemaForm';
import { mutationRenderers } from '../pages/MutationEdit';

/**
 * Guard against a widget being declared server-side with no renderer able to
 * claim it.
 *
 * This is the failure that shipped in 092e1effeb: four new widgets were
 * registered only in the view-mode registry, so in edit mode JsonForms found
 * no matching tester and silently fell back to RowControlRenderer. A field
 * declaring `widget: "vocabularySelect"` rendered as a plain text input.
 * Nothing was logged, the wire payload was correct, and typecheck, lint and
 * the whole Java suite passed with every widget unreachable.
 *
 * The uiSchemas are generated in Java, so the widget names live in the
 * committed schema snapshots; the renderers live here. This test is the one
 * place the two sides meet.
 */

const SNAPSHOT_DIR = path.resolve(__dirname, '../../../../../test/resources/zirc/snapshot');

/**
 * Which registries each form's uiSchema is rendered by. There are four,
 * because the submission page, the mutation page, the inline aggregate
 * editors and view mode each mount a different set — this table is the only
 * written-down statement of that, which is itself half the value of the test.
 */
const FORMS: Array<{ snapshot: string; registries: Array<[string, JsonFormsRendererRegistryEntry[]]> }> = [
    { snapshot: 'submission', registries: [['submissionRenderers', submissionRenderers]] },
    // MutationsListRenderer re-renders the mutation schema in view mode.
    { snapshot: 'mutation', registries: [['mutationRenderers', mutationRenderers], ['aggregateRenderers', aggregateRenderers]] },
    // The inline aggregate editors: ZircEntityEditor for edit, the sibling
    // list renderers for view.
    { snapshot: 'lesion', registries: [['fieldRenderers', fieldRenderers], ['aggregateRenderers', aggregateRenderers]] },
    { snapshot: 'assay', registries: [['fieldRenderers', fieldRenderers], ['aggregateRenderers', aggregateRenderers]] },
    { snapshot: 'gene', registries: [['fieldRenderers', fieldRenderers], ['aggregateRenderers', aggregateRenderers]] },
    { snapshot: 'phenotype', registries: [['fieldRenderers', fieldRenderers], ['aggregateRenderers', aggregateRenderers]] },
];

/** A widget name no renderer should ever claim, used to unmask generic testers. */
const SENTINEL = '__no_such_widget__';

function probe(widget: string) {
    return { type: 'Control', scope: '#/properties/probe', options: { widget } };
}

const PROBE_SCHEMA = { type: 'object', properties: { probe: { type: 'string' } } };
const PROBE_CONTEXT = { rootSchema: PROBE_SCHEMA, config: {} };

/**
 * True when `entry` claims this widget *because of the widget name*.
 *
 * The second condition is what makes the check meaningful: RowControlRenderer
 * matches any string Control, so it would "match" every probe. Requiring the
 * entry to reject the sentinel isolates renderers whose tester actually keys
 * on `options.widget`.
 */
function claimsWidget(entry: JsonFormsRendererRegistryEntry, widget: string): boolean {
    const matches = (w: string) =>
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        entry.tester(probe(w) as any, PROBE_SCHEMA as any, PROBE_CONTEXT as any) !== NOT_APPLICABLE;
    return matches(widget) && !matches(SENTINEL);
}

/** Every distinct `options.widget` value anywhere in a uiSchema tree. */
function widgetsIn(node: unknown, found: Set<string> = new Set()): Set<string> {
    if (Array.isArray(node)) {
        for (const child of node) {widgetsIn(child, found);}
        return found;
    }
    if (node && typeof node === 'object') {
        const n = node as Record<string, unknown>;
        const opts = n.options as Record<string, unknown> | undefined;
        if (opts && typeof opts.widget === 'string') {found.add(opts.widget);}
        for (const value of Object.values(n)) {widgetsIn(value, found);}
    }
    return found;
}

describe('widget registry', () => {
    it('finds the committed schema snapshots', () => {
        assert.ok(
            fs.existsSync(SNAPSHOT_DIR),
            `snapshot directory not found at ${SNAPSHOT_DIR} — if the tests or snapshots moved, `
            + 'fix the path rather than deleting this test; a silently empty run defeats the guard.',
        );
    });

    for (const { snapshot, registries } of FORMS) {
        it(`every widget in ${snapshot}.form-schema.json has a renderer`, () => {
            const file = path.join(SNAPSHOT_DIR, `${snapshot}.form-schema.json`);
            const parsed = JSON.parse(fs.readFileSync(file, 'utf8'));
            const widgets = widgetsIn(parsed.uiSchema);

            // Not an assertion about count -- just proof the walk found the
            // uiSchema at all, so a shape change can't turn this into a no-op.
            assert.ok(widgets.size > 0, `no widgets found in ${snapshot}.form-schema.json`);

            for (const widget of widgets) {
                for (const [name, registry] of registries) {
                    assert.ok(
                        registry.some((entry) => claimsWidget(entry, widget)),
                        `${snapshot}.form-schema.json declares options.widget "${widget}", but no renderer in `
                        + `${name} claims it. JsonForms will fall back to the default renderer and the field `
                        + 'will render as a plain input with nothing logged. Register the renderer in that list.',
                    );
                }
            }
        });
    }

    it('the sentinel is claimed by nobody', () => {
        // If some renderer ever matches an arbitrary widget name, claimsWidget
        // returns false for everything and the whole suite passes vacuously.
        for (const [name, registry] of [
            ['fieldRenderers', fieldRenderers],
            ['aggregateRenderers', aggregateRenderers],
            ['submissionRenderers', submissionRenderers],
            ['mutationRenderers', mutationRenderers],
        ] as Array<[string, JsonFormsRendererRegistryEntry[]]>) {
            for (const entry of registry) {
                assert.ok(
                    !claimsWidget(entry, SENTINEL),
                    `a renderer in ${name} claims the sentinel widget name, which would make this suite vacuous`,
                );
            }
        }
    });
});
