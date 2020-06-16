export const makeId = string => (
    string.toLowerCase().replace(/[^A-Za-z0-9]/g, '-')
);

export const stringToFunction = getter => (
    typeof getter === 'string' ? o => o[getter] : getter
);

export const isEmptyObject = obj => {
    if (obj == null) {
        return true;
    }
    return Object.keys(obj).length === 0 && obj.constructor === Object;
};
