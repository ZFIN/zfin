import { bool, object, shape } from 'prop-types';

export const fetchType = shape({
    pending: bool,
    rejected: bool,
    reason: object,
    fulfilled: bool,
    value: object,
});
