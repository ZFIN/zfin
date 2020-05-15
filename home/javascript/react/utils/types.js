import {
    bool,
    func,
    node,
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
});
