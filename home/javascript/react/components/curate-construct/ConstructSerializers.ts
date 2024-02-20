import {Cassette, ConstructName} from "./ConstructTypes";

function serializeCassetteList(cassetteList: Cassette[]): string {
    return JSON.stringify(cassetteList);
}

function serializeConstructName(constructName: ConstructName): string {
    return JSON.stringify(constructName);
}

export {serializeCassetteList, serializeConstructName};
