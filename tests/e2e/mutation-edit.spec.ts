import {expect, test} from '@playwright/test';
import {createFreshSubmission, login} from './fixtures/auth';

test.describe('Mutation edit (schema-driven)', () => {
    let mutationId: number;

    test.beforeEach(async ({page}) => {
        await login(page);
        ({mutationId} = await createFreshSubmission(page));
        await page.goto(`/action/zirc/mutation/${mutationId}/edit`);
    });

    test('left navigation lists Lethality immediately before Publications', async ({page}) => {
        // The sidebar is rendered by the JSP; this guards against
        // schema/JSP drift since the schema renders Lethality second-to-last.
        const labels = await page.locator('nav a, .secondary-nav a, .nav a').allTextContents();
        const compact = labels.map(s => s.trim()).filter(Boolean);
        const lethality = compact.indexOf('Lethality');
        const publications = compact.indexOf('Publications');
        expect(lethality).toBeGreaterThanOrEqual(0);
        expect(publications).toBeGreaterThan(lethality);
        expect(publications - lethality).toBe(1);
    });

    test('lesion type-picker shows the 7 lesion types', async ({page}) => {
        await page.locator('section#lesions button:has-text("+ Add")').click();
        const modal = page.locator('.modal-content', {hasText: 'Pick a lesion type'});
        await expect(modal).toBeVisible();
        for (const label of [
            'Point mutation', 'Deletion', 'Insertion', 'Indel (delins)',
            'Transgene', 'Other', 'Unknown',
        ]) {
            await expect(modal.locator(`button:has-text("${label}")`)).toBeVisible();
        }
        await modal.locator('button:has-text("Cancel")').click();
        await expect(modal).toBeHidden();
    });

    test('Deletion lesion shows the deletion field set', async ({page}) => {
        await page.locator('section#lesions button:has-text("+ Add")').click();
        await page.locator('.modal-content button:has-text("Deletion")').click();
        // New rows stay expanded; no Edit click needed.
        const labels = await page.locator('section#lesions fieldset tr th').allTextContents();
        const compact = labels.map(s => s.trim());
        // Deletion's matrix entry: lesionSizeBp, deletedSequence,
        // locationInline, hasLargeVariant, mutatedAminoAcids,
        // mutatedAminoAcidsHgvs, additionalInfo (plus the type select).
        expect(compact).toContain('Lesion size');
        expect(compact).toContain('Deleted sequence');
        expect(compact).toContain('Location (inline)');
        expect(compact).toContain('Large variant?');
        expect(compact).not.toContain('Nucleotide change');
        expect(compact).not.toContain('Inserted sequence');
        // Flank fields hidden until Large variant=Yes.
        expect(compact).not.toContain('5′ flank(i)');
    });

    test('hasLargeVariant=Yes reveals 5′ and 3′ flanks', async ({page}) => {
        await page.locator('section#lesions button:has-text("+ Add")').click();
        await page.locator('.modal-content button:has-text("Deletion")').click();
        await page.locator('section#lesions input#fr-hasLargeVariant-true').click();
        await page.waitForTimeout(500);
        const labels = await page.locator('section#lesions fieldset tr th').allTextContents();
        const compact = labels.map(s => s.trim());
        expect(compact.find(l => l.startsWith('5′ flank'))).toBeTruthy();
        expect(compact.find(l => l.startsWith('3′ flank'))).toBeTruthy();
    });

    test('RFLP assay shows the enzyme-cleaves checkboxes (checkbox field type)', async ({page}) => {
        await page.locator('section#genotyping-assays button:has-text("+ Add")').click();
        await page.locator('.modal-content button:has-text("RFLP")').click();
        const wt = page.locator('section#genotyping-assays input#fr-enzymeCleavesWt');
        const mut = page.locator('section#genotyping-assays input#fr-enzymeCleavesMut');
        await expect(wt).toBeVisible();
        await expect(wt).toHaveAttribute('type', 'checkbox');
        await expect(mut).toBeVisible();
        await expect(mut).toHaveAttribute('type', 'checkbox');
    });
});
