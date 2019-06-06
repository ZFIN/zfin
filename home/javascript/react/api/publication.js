import http from '../utils/http';

export const getStatuses = () => {
    return http.get('/action/publication/statuses');
};

export const getCurators = () => {
    return http.get('/action/publication/curators');
};

export const getLocations = () => {
    return http.get('/action/publication/locations');
};

export const searchPubStatus = (params) => {
    return http.get('/action/publication/search-status', params);
};

export const updateStatus = (status, checkOwner) => {
    const endpoint = '/status' + (checkOwner ? '?checkOwner=true' : '');
    return http.post('/action/publication/' + status.pubZdbID + endpoint, status);
};

export const getChecklist = (pubId) => {
    return http.get(`/action/publication/${pubId}/checklist`);
};

export const addChecklistEntry = (pubId, task) => {
    return http.post(`/action/publication/${pubId}/checklist`, {task});
};

export const deleteChecklistEntry = (entryId) => {
    return http.delete(`/action/publication/checklist/${entryId}`);
};
