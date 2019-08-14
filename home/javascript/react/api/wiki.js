import http from '../utils/http';

export const getBlogPosts = (space) => {
    const params = {
        cql: `space=${space} AND type=blogpost ORDER BY created desc`,
        limit: 10,
        expand: 'history',
    };
    return http.get('https://@WIKI_HOST@/rest/api/content/search', params);
};
