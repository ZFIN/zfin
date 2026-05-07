// ESLint 9 flat config. Uses @eslint/eslintrc's FlatCompat so the existing
// rule set lives unchanged in one place; switch to native flat config on a
// follow-up when we're ready to rewrite the rule list.
const { FlatCompat } = require('@eslint/eslintrc');
const js = require('@eslint/js');
const path = require('path');

const compat = new FlatCompat({
    baseDirectory: __dirname,
    recommendedConfig: js.configs.recommended,
    allConfig: js.configs.all,
});

module.exports = [
    {
        ignores: ['node_modules/**', 'home/dist/**', 'build/**'],
    },
    ...compat.config({
        parser: '@typescript-eslint/parser',
        env: {
            browser: true,
            es2021: true,
            node: true,
        },
        parserOptions: {
            ecmaVersion: 12,
            sourceType: 'module',
            ecmaFeatures: {
                jsx: true,
            },
        },
        extends: [
            'eslint:recommended',
            'plugin:react/recommended',
            'plugin:@typescript-eslint/eslint-recommended',
            'plugin:@typescript-eslint/recommended',
        ],
        plugins: ['react', '@typescript-eslint'],
        settings: {
            react: {
                version: 'detect',
            },
        },
        globals: {
            $: 'readonly',
            jQuery: 'readonly',
        },
        rules: {
            curly: 'error',
            eqeqeq: ['error', 'always', { null: 'ignore' }],
            indent: ['warn', 4, { ignoredNodes: ['TemplateLiteral'] }],
            'jsx-quotes': ['error', 'prefer-single'],
            'no-alert': 'error',
            'no-console': ['warn', { allow: ['warn', 'error'] }],
            'no-var': 'error',
            'react/display-name': 'off',
            'react/jsx-closing-bracket-location': ['warn', 'line-aligned'],
            'react/jsx-first-prop-new-line': 'warn',
            'react/jsx-indent': ['warn', 4],
            'react/jsx-indent-props': ['warn', 4],
            'react/jsx-max-props-per-line': ['warn', { when: 'multiline' }],
            'react/self-closing-comp': 'error',
            'react/prop-types': ['error', { skipUndeclared: true }],
            quotes: ['error', 'single'],
        },
    }),
];
