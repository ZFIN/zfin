export const makeId = string => {
    if (!string) {
        return;
    }
    return string.toLowerCase().replace(/[^A-Za-z0-9]/g, '-');
};

export const stringToFunction = getter => (
    typeof getter === 'string' ? o => o[getter] : getter
);

export const isEmptyObject = obj => {
    if (obj == null) {
        return true;
    }
    return Object.keys(obj).length === 0 && obj.constructor === Object;
};

export function parseDate(string) {
    // matches string in YYYY-MM-DD format and returns a Date object. The Date constructor can
    // identify this format but it interprets it as a UTC time instead of local timezone
    const match = string.match(/^(\d{4})-(\d{2})-(\d{2})$/);
    if (!match) {
        return;
    }
    const year = parseInt(match[1], 10);
    const month = parseInt(match[2], 10);
    const date = parseInt(match[3], 10);
    return new Date(year, month - 1, date); // month is 0-indexed!
}

export function stringToBool(str) {
    return str && str.toLowerCase() === 'true';
}
