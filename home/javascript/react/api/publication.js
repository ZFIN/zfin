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

export const getStatus = (pubId) => {
    return http.get(`/action/publication/${pubId}/status`);
};

export const updateStatus = (pubId, status, checkOwner) => {
    const endpoint = '/status' + (checkOwner ? '?checkOwner=true' : '');
    return http.post('/action/publication/' + pubId + endpoint, status);
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

export const getIndexed = (pubId) => {
    return http.get('/action/publication/' + pubId + '/indexed');
};

export const updateIndexed = (pubId, indexed) => {
    return http.post('/action/publication/' + pubId + '/indexed', indexed);
};

export const getNotes = (pubId) => {
    return http.get(`/action/publication/${pubId}/notes`);
};

export const getTopics = (pubId) => {
    return http.get(`/action/publication/${pubId}/topics`);
};

export const addTopic = (pubId, topic) => {
    return http.post('/action/publication/' + pubId + '/topics', topic);
};

export const updateTopic = (topicId, topic) => {
    return http.post('/action/publication/topics/' + topicId, topic);
};

export const validate = (pubId) => {
    return http.post(`/action/publication/${pubId}/validate`, {});
};
