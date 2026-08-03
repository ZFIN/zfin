import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
    cassettesToSimplifiedCassettes,
    simplifiedCassettesToCassettes,
    normalizeConstructComponents,
    normalizeConstructCassette,
    normalizeSimplifiedCassettes,
    Cassette,
    ConstructComponent,
    SimplifiedCassette
} from '../ConstructTypes';

function makeComponent(value: string, separator = ''): ConstructComponent {
    return {
        id: null,
        name: null,
        label: '',
        value,
        url: null,
        category: null,
        type: null,
        separator
    };
}

describe('cassettesToSimplifiedCassettes', () => {
    it('converts a cassette with separators to simplified format', () => {
        const cassette: Cassette = {
            promoter: [
                makeComponent('hsp70l', '-'),
                makeComponent('GFP', '')
            ],
            coding: [
                makeComponent('EGFP', '-'),
                makeComponent('CAAX', '')
            ]
        };
        const result = cassettesToSimplifiedCassettes([cassette]);
        assert.deepEqual(result, [{
            cassetteNumber: undefined,
            promoter: ['hsp70l', '-', 'GFP'],
            coding: ['EGFP', '-', 'CAAX']
        }]);
    });

    it('omits empty separators in simplified format', () => {
        const cassette: Cassette = {
            promoter: [makeComponent('hsp70l', ''), makeComponent('GFP', '')],
            coding: []
        };
        const result = cassettesToSimplifiedCassettes([cassette]);
        assert.deepEqual(result[0].promoter, ['hsp70l', 'GFP']);
    });

    it('handles empty cassette', () => {
        const cassette: Cassette = { promoter: [], coding: [] };
        const result = cassettesToSimplifiedCassettes([cassette]);
        assert.deepEqual(result[0].promoter, []);
        assert.deepEqual(result[0].coding, []);
    });

    it('handles multiple cassettes', () => {
        const cassettes: Cassette[] = [
            { promoter: [makeComponent('hsp70l', '-')], coding: [makeComponent('EGFP', '')] },
            { promoter: [makeComponent('ubi', '')], coding: [makeComponent('mCherry', '')] }
        ];
        const result = cassettesToSimplifiedCassettes(cassettes);
        assert.equal(result.length, 2);
        assert.deepEqual(result[0].promoter, ['hsp70l', '-']);
        assert.deepEqual(result[1].coding, ['mCherry']);
    });
});

describe('simplifiedCassettesToCassettes', () => {
    it('converts simplified format back to cassettes with separators', () => {
        const simplified: SimplifiedCassette = {
            promoter: ['hsp70l', '-', 'GFP'],
            coding: ['EGFP']
        };
        const result = simplifiedCassettesToCassettes([simplified]);
        assert.equal(result[0].promoter.length, 2);
        assert.equal(result[0].promoter[0].value, 'hsp70l');
        assert.equal(result[0].promoter[0].separator, '-');
        assert.equal(result[0].promoter[1].value, 'GFP');
        assert.equal(result[0].promoter[1].separator, '');
    });

    it('recognizes all separator types: dash, comma, period', () => {
        const simplified: SimplifiedCassette = {
            promoter: ['a', '-', 'b', ',', 'c', '.', 'd'],
            coding: []
        };
        const result = simplifiedCassettesToCassettes([simplified]);
        assert.equal(result[0].promoter[0].separator, '-');
        assert.equal(result[0].promoter[1].separator, ',');
        assert.equal(result[0].promoter[2].separator, '.');
        assert.equal(result[0].promoter[3].separator, '');
    });

    it('roundtrips: cassette -> simplified -> cassette preserves data', () => {
        const original: Cassette = {
            promoter: [makeComponent('hsp70l', '-'), makeComponent('GFP', '')],
            coding: [makeComponent('EGFP', ','), makeComponent('CAAX', '')]
        };
        const simplified = cassettesToSimplifiedCassettes([original]);
        const roundtripped = simplifiedCassettesToCassettes(simplified);
        assert.equal(roundtripped[0].promoter[0].value, 'hsp70l');
        assert.equal(roundtripped[0].promoter[0].separator, '-');
        assert.equal(roundtripped[0].promoter[1].value, 'GFP');
        assert.equal(roundtripped[0].coding[0].value, 'EGFP');
        assert.equal(roundtripped[0].coding[0].separator, ',');
        assert.equal(roundtripped[0].coding[1].value, 'CAAX');
    });
});

describe('normalizeConstructComponents', () => {
    it('clears separator on the last component', () => {
        const items: ConstructComponent[] = [
            makeComponent('a', '-'),
            makeComponent('b', '-'),
            makeComponent('c', '-')
        ];
        const result = normalizeConstructComponents(items);
        assert.equal(result[0].separator, '-');
        assert.equal(result[1].separator, '-');
        assert.equal(result[2].separator, '');
    });

    it('handles single component', () => {
        const items: ConstructComponent[] = [makeComponent('a', '-')];
        const result = normalizeConstructComponents(items);
        assert.equal(result[0].separator, '');
    });

    it('handles empty array', () => {
        const result = normalizeConstructComponents([]);
        assert.deepEqual(result, []);
    });
});

describe('normalizeConstructCassette', () => {
    it('normalizes both promoter and coding arrays', () => {
        const cassette: Cassette = {
            promoter: [makeComponent('a', '-'), makeComponent('b', '-')],
            coding: [makeComponent('c', ','), makeComponent('d', ',')]
        };
        const result = normalizeConstructCassette(cassette);
        assert.equal(result.promoter[1].separator, '');
        assert.equal(result.coding[1].separator, '');
        // first items keep their separators
        assert.equal(result.promoter[0].separator, '-');
        assert.equal(result.coding[0].separator, ',');
    });
});

describe('normalizeSimplifiedCassettes', () => {
    it('removes leading comma from non-first cassette promoters', () => {
        const cassettes: SimplifiedCassette[] = [
            { promoter: ['a'], coding: ['b'] },
            { promoter: [',', 'c'], coding: ['d'] }
        ];
        const result = normalizeSimplifiedCassettes(cassettes);
        assert.deepEqual(result[0].promoter, ['a']);
        assert.deepEqual(result[1].promoter, ['c']);
    });

    it('does not remove comma from first cassette', () => {
        const cassettes: SimplifiedCassette[] = [
            { promoter: [',', 'a'], coding: [] }
        ];
        const result = normalizeSimplifiedCassettes(cassettes);
        assert.deepEqual(result[0].promoter, [',', 'a']);
    });
});
