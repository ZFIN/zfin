import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { valueToInputString } from '../FormPrimitives';

describe('valueToInputString', () => {
    it('returns empty string for null', () => {
        assert.equal(valueToInputString(null), '');
    });

    it('returns empty string for undefined', () => {
        assert.equal(valueToInputString(undefined), '');
    });

    it('returns "true"/"false" for booleans', () => {
        assert.equal(valueToInputString(true), 'true');
        assert.equal(valueToInputString(false), 'false');
    });

    it('passes strings through unchanged', () => {
        assert.equal(valueToInputString(''), '');
        assert.equal(valueToInputString('hello'), 'hello');
        assert.equal(valueToInputString('  whitespace  '), '  whitespace  ');
    });
});
