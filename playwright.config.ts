import {defineConfig, devices} from '@playwright/test';

/**
 * Playwright config for the ZIRC schema-driven editor end-to-end tests.
 *
 * The tests hit the local dev environment (cell-mac.zfin.org by default).
 * The self-signed cert there is bypassed via `ignoreHTTPSErrors`. Override
 * the base URL by setting `PLAYWRIGHT_BASE_URL` if you want to point at a
 * different host (e.g. cell.zfin.org for a sanity check against legacy).
 *
 * Login credentials default to env vars; the helpers in
 * tests/e2e/fixtures/auth.ts handle the form. Don't check real prod creds
 * into the repo -- the defaults below are for the shared `staylor`
 * dev/test user only.
 */
export default defineConfig({
    testDir: './tests/e2e',
    fullyParallel: false, // shared local DB; avoid cross-test contention
    forbidOnly: !!process.env.CI,
    retries: process.env.CI ? 2 : 0,
    workers: 1,
    reporter: process.env.CI ? 'github' : 'list',

    use: {
        baseURL: process.env.PLAYWRIGHT_BASE_URL ?? 'https://cell-mac.zfin.org',
        ignoreHTTPSErrors: true,
        trace: 'retain-on-failure',
        screenshot: 'only-on-failure',
    },

    projects: [
        {
            name: 'chromium',
            use: {...devices['Desktop Chrome']},
        },
    ],
});
