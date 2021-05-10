import http from '../utils/http';

export const getBlogPosts = (space, options = {}) => {
    const params = {
        cql: `space=${space} AND type=blogpost${options.query ? ` AND ${options.query}` : ''} ORDER BY created desc`,
        limit: 5,
        expand: 'history',
    };
    return http.get(`https://${process.env.WIKI_HOST}/rest/api/content/search`, params);
};

export const getPosts = (space = {}) => {
    const params = {
        limit: 5,
        page: 1,
    };
    return http.get(`/action/api/wiki/${space}`, params);
};

export const getLink = (link) => {
    return http.get(`https://${process.env.WIKI_HOST}${link}`);
};
