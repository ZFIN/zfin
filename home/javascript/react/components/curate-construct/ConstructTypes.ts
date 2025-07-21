interface ConstructName {
    typeAbbreviation: string;
    prefix: string;
    cassettes: Cassette[];
}

//a type for the construct name that is returned from the server or sent to the server
type ConstructNameDTO = {
    type: string;
    prefix: string;
    cassettes: SimplifiedCassette[];
}

type ConstructFormDTO = {
    constructNameObject: ConstructNameDTO;
    constructAlias: string;
    constructSequence: string;
    constructComments: string;
    constructCuratorNote: string;
    pubZdbID: string;
}


type EditConstructFormDTO = {
    constructName: ConstructNameDTO;
    synonyms: MarkerLabelAndZdbId[];
    sequences: MarkerLabelAndZdbId[];
    notes: MarkerLabelAndZdbId[];
    publicNote: string;
    publicationZdbID: string;
}

interface Cassette {
    cassetteNumber?: number;
    promoter: ConstructComponent[];
    coding: ConstructComponent[];
}

type ConstructComponent = {
    id: null | string;
    name: null | string;
    label: string;
    value: string;
    url: null | string;
    category: null | string;
    type: null | string;
    separator: string;
};

interface SimplifiedCassette {
    cassetteNumber?: number;
    promoter: string[];
    coding: string[];
}

function cassetteToSimplifiedCassette(cassette: Cassette): SimplifiedCassette {
    const promoter = [];
    const coding = [];
    for (const component of cassette.promoter) {
        promoter.push(component.value);
        if (component.separator && component.separator !== '') {
            promoter.push(component.separator);
        }
    }
    for (const component of cassette.coding) {
        coding.push(component.value);
        if (component.separator && component.separator !== '') {
            coding.push(component.separator);
        }
    }
    return {
        cassetteNumber: cassette.cassetteNumber,
        promoter: promoter,
        coding: coding
    };
}

function cassettesToSimplifiedCassettes(cassettes: Cassette[]): SimplifiedCassette[] {
    return cassettes.map(cassetteToSimplifiedCassette);
}

function simplifiedCassetteToCassette(simplifiedCassette: SimplifiedCassette): Cassette {
    const promoter = [];
    const coding = [];
    for (const component of simplifiedCassette.coding) {
        if (coding.length > 0 && (component === '.' || component === ',' || component === '-')) {
            coding[coding.length - 1].separator = component;
            continue;
        }
        coding.push({
            id: null,
            name: null,
            label: '',
            value: component,
            url: null,
            category: null,
            type: null,
            separator: ''
        });
    }
    for (const component of simplifiedCassette.promoter) {
        if (promoter.length > 0 && (component === '.' || component === ',' || component === '-')) {
            promoter[promoter.length - 1].separator = component;
            continue;
        }
        promoter.push({
            id: null,
            name: null,
            label: '',
            value: component,
            url: null,
            category: null,
            type: null,
            separator: ''
        });
    }
    return {
        cassetteNumber: simplifiedCassette.cassetteNumber,
        promoter: promoter,
        coding: coding
    };
}

function simplifiedCassettesToCassettes(simplifiedCassettes: SimplifiedCassette[]): Cassette[] {
    return simplifiedCassettes.map(simplifiedCassetteToCassette);
}

function typeAbbreviationToType(typeAbbreviation: string): string {
    switch (typeAbbreviation) {
    case 'Tg':
        return 'TGCONSTRCT';
    case 'Et':
        return 'ETCONSTRCT';
    case 'Gt':
        return 'GTCONSTRCT';
    case 'Pt':
        return 'PTCONSTRCT';
    default:
        return '';
    }
}

function normalizeSimplifiedCassettes(simplifiedCassettes) {
    return simplifiedCassettes.map((cassette, i) => ({
        cassetteNumber: cassette.cassetteNumber,
        promoter: cassette.promoter.filter((component, j) => !(j === 0 && i > 0 && component === ',')),
        coding: [...cassette.coding]
    }));
}

function normalizeConstructComponents(constructComponents : ConstructComponent[]) {
    return constructComponents.map((item, index) => {
        if (index === constructComponents.length - 1) {
            return {...item, separator: ''};
        }
        return item;
    });
}

function normalizeConstructCassette(cassette: Cassette) {
    return {
        cassetteNumber: cassette.cassetteNumber,
        promoter: normalizeConstructComponents(cassette.promoter),
        coding: normalizeConstructComponents(cassette.coding)
    };
}

type MarkerLabelAndZdbId = {
    label: string;
    zdbID: string;
}

type MarkerNameAndZdbId = {
    name: string;
    zdbID: string;
}

export {ConstructName, Cassette, ConstructComponent, SimplifiedCassette, cassettesToSimplifiedCassettes, typeAbbreviationToType, MarkerLabelAndZdbId, MarkerNameAndZdbId, ConstructNameDTO, ConstructFormDTO, EditConstructFormDTO, simplifiedCassettesToCassettes, normalizeSimplifiedCassettes, normalizeConstructComponents, normalizeConstructCassette};
