interface ConstructName {
    typeAbbreviation: string;
    prefix: string;
    cassettes: Cassette[];
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
    let promoter = [];
    let coding = [];
    for (let component of cassette.promoter) {
        promoter.push(component.value);
        if (component.separator && component.separator !== "") {
            promoter.push(component.separator);
        }
    }
    for (let component of cassette.coding) {
        coding.push(component.value);
        if (component.separator && component.separator !== "") {
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

function typeAbbreviationToType(typeAbbreviation: string): string {
    switch (typeAbbreviation) {
        case "Tg": return "TGCONSTRCT";
        case "Et": return "ETCONSTRCT";
        case "Gt": return "GTCONSTRCT";
        case "Pt": return "PTCONSTRCT";
        default: return "";
    }
}

export {ConstructName, Cassette, ConstructComponent, SimplifiedCassette, cassettesToSimplifiedCassettes, typeAbbreviationToType};