import http from '../utils/http';

export const getBlogPosts = (space, options = {}) => {
    const params = {
        cql: `space=${space} AND type=blogpost${options.query ? ` AND ${options.query}` : ''} ORDER BY created desc`,
        limit: 5,
        expand: 'history',
    };
    return http.get('https://wiki.zfin.org/rest/api/content/search', params);
};

export const getPosts = (space = {}) => {
    const params = {
        limit: 2,
    };
    return http.get(`/action/api/wiki/${space}`, params);
};

export const getMeetingLink = (link) => {
    return http.get(link);
};

export const getLink = (link) => {
    return http.get(`https://@WIKI_HOST@${link}`);
};
