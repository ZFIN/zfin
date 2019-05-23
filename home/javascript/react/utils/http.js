// these methods encapsulate calls to the jquery ajax method in case we
// want to change which library we use to do http requests

export const get = (url, data) => {
    return $.ajax({
        type: 'GET',
        url,
        data,
        dataType: 'json',
    });
};

export const post = (url, data) => {
    return $.ajax({
        type: 'POST',
        url,
        data: JSON.stringify(data),
        dataType: 'json',
        contentType: 'application/json',
    });
};
