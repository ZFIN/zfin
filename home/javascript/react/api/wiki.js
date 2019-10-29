import http from '../utils/http';

export const getBlogPosts = (space, options = {}) => {
    const params = {
        cql: `space=${space} AND type=blogpost${options.query ? ` AND ${options.query}` : ''} ORDER BY created desc`,
        limit: 5,
        expand: 'history',
    };
    return http.get('https://@WIKI_HOST@/rest/api/content/search', params);
};

export const getLink = (link) => {
    return http.get(`https://@WIKI_HOST@${link}`);
};
