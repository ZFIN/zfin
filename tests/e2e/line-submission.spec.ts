import {expect, test} from '@playwright/test';
import {createFreshSubmission, login} from './fixtures/auth';

test.describe('Line submission edit (schema-driven)', () => {
    let submissionId: string;

    test.beforeEach(async ({page}) => {
        await login(page);
        ({submissionId} = await createFreshSubmission(page));
        await page.goto(`/action/zirc/line-submission/${submissionId}/edit`);
    });

    test('Overview renders ID as read-only and Name as a text input', async ({page}) => {
        const overview = page.locator('section#overview');
        await expect(overview.locator('th', {hasText: 'ID'}).locator('xpath=../td/code')).toHaveText(submissionId);
        await expect(overview.locator('input#fr-name')).toBeVisible();
        await expect(overview.locator('input#fr-previousNames')).toBeVisible();
    });

    test('scalar autosave round-trips through /patch', async ({page}) => {
        // ls_name has a unique constraint, so suffix per-run to avoid
        // collisions across repeated test runs against the same DB.
        const uniqueName = `e2e-scalar-${Date.now()}`;
        const name = page.locator('input#fr-name');
        await name.fill(uniqueName);
        // Wait for the actual patch POST to land before reloading;
        // .blur() alone doesn't always trigger React's onBlur reliably.
        const patchResponse = page.waitForResponse(r =>
            r.url().endsWith('/line-submission/patch') && r.request().method() === 'POST');
        await page.keyboard.press('Tab');
        await patchResponse;
        await page.reload();
        await expect(page.locator('input#fr-name')).toHaveValue(uniqueName);
        await expect(page).toHaveTitle(new RegExp(uniqueName));
    });

    test('select-with-other (Maternal) reveals free-text and persists', async ({page}) => {
        // Pick "Other"; the renderer should reveal the free-text input.
        await page.locator('select#fr-maternalBackground').selectOption({label: 'Other'});
        const other = page.locator('input#fr-maternalBackground-other');
        await expect(other).toBeVisible();
        await other.fill('custom-background-e2e');
        await other.blur();
        await page.waitForTimeout(800);
        await page.reload();
        // On reload the renderer's value-shape heuristic should re-enter
        // Other mode and prefill the typed value.
        await expect(page.locator('select#fr-maternalBackground')).toHaveValue('__other');
        await expect(page.locator('input#fr-maternalBackground-other'))
            .toHaveValue('custom-background-e2e');
    });

    test('multi-checkbox-with-other (Reasons) persists canonical + other', async ({page}) => {
        await page.locator('input#fr-reasons-interesting_gene').check();
        await page.locator('input#fr-reasons-other').check();
        const otherText = page.locator('input#fr-reasons-other-text');
        await expect(otherText).toBeVisible();
        await otherText.fill('e2e-other-reason');
        await otherText.blur();
        await page.waitForTimeout(800);
        await page.reload();
        await expect(page.locator('input#fr-reasons-interesting_gene')).toBeChecked();
        await expect(page.locator('input#fr-reasons-other')).toBeChecked();
        await expect(page.locator('input#fr-reasons-other-text'))
            .toHaveValue('e2e-other-reason');
    });

    test('"+ Add mutation" works on a brand-new submission (no zdbID yet)', async ({page}) => {
        await page.goto('/action/zirc/line-submission/new');
        const add = page.locator('a:has-text("+ Add mutation")');
        await expect(add).toHaveAttribute('href', '/action/zirc/line-submission/new-with-mutation');
        await add.click();
        // Lands on the mutation editor with a freshly-assigned id.
        await expect(page).toHaveURL(/\/mutation\/\d+\/edit$/);
    });
});
