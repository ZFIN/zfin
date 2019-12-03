import http from '../utils/http';

export const getBlogPosts = (space, options = {}) => {
    const params = {
        cql: `space=${space} AND type=blogpost${options.query ? ` AND ${options.query}` : ''} ORDER BY created desc`,
        limit: 5,
        expand: 'history',
    };
    return http.get('https://wiki.zfin.org/rest/api/content/search', params);
};

export const getPosts = (space, options = {}) => {
    const params = {
        limit: 5,
        expand: 'history',
    };
    return http.get('http://schlapp/action/api/wiki/meetings', params);
};

export const getLink = (link) => {
    return http.get(`https://@WIKI_HOST@${link}`);
};
