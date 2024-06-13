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

type MarkerNameAndZdbId = {
    label: string;
    zdbID: string;
}

export {ConstructName, Cassette, ConstructComponent, SimplifiedCassette, cassettesToSimplifiedCassettes, typeAbbreviationToType, MarkerNameAndZdbId, ConstructNameDTO, ConstructFormDTO, simplifiedCassettesToCassettes};