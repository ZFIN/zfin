export const getStatuses = () => {
    return $.get('/action/publication/statuses');
};

export const getCurators = () => {
    return $.get('/action/publication/curators');
};

export const searchPubStatus = (params) => {
    return $.get('/action/publication/search-status', params);
};
