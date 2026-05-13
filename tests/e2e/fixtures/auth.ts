import {Page, expect} from '@playwright/test';

const DEFAULT_USERNAME = process.env.E2E_USERNAME ?? 'staylor';
const DEFAULT_PASSWORD = process.env.E2E_PASSWORD ?? 'honeymonkeybread';

/**
 * Log in via the ZFIN login form. Idempotent: if already logged in (no
 * username field visible) returns immediately. Tests should call this in
 * their `beforeEach` rather than depending on a global setup so a
 * canceled run doesn't leave subsequent tests in an authenticated state
 * that doesn't reflect the actual flow.
 */
export async function login(
    page: Page,
    username: string = DEFAULT_USERNAME,
    password: string = DEFAULT_PASSWORD,
): Promise<void> {
    await page.goto('/action/login');
    // If we land somewhere that doesn't have the login form (already
    // signed in), there's nothing to do.
    const usernameField = page.locator('input#username');
    if (await usernameField.count() === 0) {
        return;
    }
    await usernameField.fill(username);
    await page.locator('input#password').fill(password);
    await page.locator('button[name=loginButton]').click();
    // Login lands on a member page, not the login page itself.
    await expect(page).not.toHaveURL(/\/login/);
}

/**
 * Create a fresh line submission for the current curator and return its
 * zdbID. Uses the "new with mutation" endpoint to also seed a mutation
 * under it, which a few tests need; harmless when the test only cares
 * about the submission. Each test should call this in `beforeEach` so
 * tests don't share submissions.
 */
export async function createFreshSubmission(page: Page): Promise<{
    submissionId: string;
    mutationId: number;
}> {
    await page.goto('/action/zirc/line-submission/new-with-mutation');
    // The endpoint redirects to /mutation/{id}/edit.
    await expect(page).toHaveURL(/\/mutation\/\d+\/edit$/);
    const mutationId = Number(page.url().match(/\/mutation\/(\d+)\/edit/)![1]);
    // The "Back to Submission" link gives us the parent submission id.
    const backLink = await page.locator('a:has-text("Back to Submission")').getAttribute('href');
    const submissionId = backLink!.match(/\/line-submission\/(ZDB-[^/]+)/)![1];
    return {submissionId, mutationId};
}
