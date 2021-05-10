import {
    array,
    arrayOf,
    bool,
    func,
    node,
    number,
    object,
    oneOf,
    oneOfType,
    shape,
    string,
} from 'prop-types';

export const fetchType = shape({
    pending: bool,
    rejected: bool,
    reason: object,
    fulfilled: bool,
    value: object,
});

export const columnDefinitionType = shape({
    align: oneOf(['right', 'center', 'left']),
    label: node.isRequired,
    content: oneOfType([string, func]).isRequired,
    key: string,
    filterName: string,
    filterOptions: arrayOf(string),
});

export const resultResponseType = shape({
    results: array,
    total: number,
    returnedRecords: number,
    supplementalData: object,
});

export const downloadOptionType = shape({
    format: string,
    url: string,
});

export const sortOptionType = shape({
    value: string,
    label: node,
});

export const entityType = shape({
    abbreviation: string.isRequired,
    zdbID: string.isRequired,
})

export const tableStateType = shape({
    limit: number,
    page: number,
    sortBy: string,
    filter: oneOfType([object, string]),
});

export const publicationType = shape({
    zdbID: string,
    title: string,
    shortAuthorList: string,
});
