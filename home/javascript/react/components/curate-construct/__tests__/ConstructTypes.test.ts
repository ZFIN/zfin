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
        expect(result).toEqual([{
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
        expect(result[0].promoter).toEqual(['hsp70l', 'GFP']);
    });

    it('handles empty cassette', () => {
        const cassette: Cassette = { promoter: [], coding: [] };
        const result = cassettesToSimplifiedCassettes([cassette]);
        expect(result[0].promoter).toEqual([]);
        expect(result[0].coding).toEqual([]);
    });

    it('handles multiple cassettes', () => {
        const cassettes: Cassette[] = [
            { promoter: [makeComponent('hsp70l', '-')], coding: [makeComponent('EGFP', '')] },
            { promoter: [makeComponent('ubi', '')], coding: [makeComponent('mCherry', '')] }
        ];
        const result = cassettesToSimplifiedCassettes(cassettes);
        expect(result.length).toBe(2);
        expect(result[0].promoter).toEqual(['hsp70l', '-']);
        expect(result[1].coding).toEqual(['mCherry']);
    });
});

describe('simplifiedCassettesToCassettes', () => {
    it('converts simplified format back to cassettes with separators', () => {
        const simplified: SimplifiedCassette = {
            promoter: ['hsp70l', '-', 'GFP'],
            coding: ['EGFP']
        };
        const result = simplifiedCassettesToCassettes([simplified]);
        expect(result[0].promoter.length).toBe(2);
        expect(result[0].promoter[0].value).toBe('hsp70l');
        expect(result[0].promoter[0].separator).toBe('-');
        expect(result[0].promoter[1].value).toBe('GFP');
        expect(result[0].promoter[1].separator).toBe('');
    });

    it('recognizes all separator types: dash, comma, period', () => {
        const simplified: SimplifiedCassette = {
            promoter: ['a', '-', 'b', ',', 'c', '.', 'd'],
            coding: []
        };
        const result = simplifiedCassettesToCassettes([simplified]);
        expect(result[0].promoter[0].separator).toBe('-');
        expect(result[0].promoter[1].separator).toBe(',');
        expect(result[0].promoter[2].separator).toBe('.');
        expect(result[0].promoter[3].separator).toBe('');
    });

    it('roundtrips: cassette -> simplified -> cassette preserves data', () => {
        const original: Cassette = {
            promoter: [makeComponent('hsp70l', '-'), makeComponent('GFP', '')],
            coding: [makeComponent('EGFP', ','), makeComponent('CAAX', '')]
        };
        const simplified = cassettesToSimplifiedCassettes([original]);
        const roundtripped = simplifiedCassettesToCassettes(simplified);
        expect(roundtripped[0].promoter[0].value).toBe('hsp70l');
        expect(roundtripped[0].promoter[0].separator).toBe('-');
        expect(roundtripped[0].promoter[1].value).toBe('GFP');
        expect(roundtripped[0].coding[0].value).toBe('EGFP');
        expect(roundtripped[0].coding[0].separator).toBe(',');
        expect(roundtripped[0].coding[1].value).toBe('CAAX');
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
        expect(result[0].separator).toBe('-');
        expect(result[1].separator).toBe('-');
        expect(result[2].separator).toBe('');
    });

    it('handles single component', () => {
        const items: ConstructComponent[] = [makeComponent('a', '-')];
        const result = normalizeConstructComponents(items);
        expect(result[0].separator).toBe('');
    });

    it('handles empty array', () => {
        const result = normalizeConstructComponents([]);
        expect(result).toEqual([]);
    });
});

describe('normalizeConstructCassette', () => {
    it('normalizes both promoter and coding arrays', () => {
        const cassette: Cassette = {
            promoter: [makeComponent('a', '-'), makeComponent('b', '-')],
            coding: [makeComponent('c', ','), makeComponent('d', ',')]
        };
        const result = normalizeConstructCassette(cassette);
        expect(result.promoter[1].separator).toBe('');
        expect(result.coding[1].separator).toBe('');
        // first items keep their separators
        expect(result.promoter[0].separator).toBe('-');
        expect(result.coding[0].separator).toBe(',');
    });
});

describe('normalizeSimplifiedCassettes', () => {
    it('removes leading comma from non-first cassette promoters', () => {
        const cassettes: SimplifiedCassette[] = [
            { promoter: ['a'], coding: ['b'] },
            { promoter: [',', 'c'], coding: ['d'] }
        ];
        const result = normalizeSimplifiedCassettes(cassettes);
        expect(result[0].promoter).toEqual(['a']);
        expect(result[1].promoter).toEqual(['c']);
    });

    it('does not remove comma from first cassette', () => {
        const cassettes: SimplifiedCassette[] = [
            { promoter: [',', 'a'], coding: [] }
        ];
        const result = normalizeSimplifiedCassettes(cassettes);
        expect(result[0].promoter).toEqual([',', 'a']);
    });
});
