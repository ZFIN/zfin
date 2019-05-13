import { get, post } from '../utils/http';

export const getStatuses = () => {
    return get('/action/publication/statuses');
};

export const getCurators = () => {
    return get('/action/publication/curators');
};

export const searchPubStatus = (params) => {
    return get('/action/publication/search-status', params);
};

export const updateStatus = (status, checkOwner) => {
    const endpoint = '/status' + (checkOwner ? '?checkOwner=true' : '');
    return post('/action/publication/' + status.pubZdbID + endpoint, status);
};
