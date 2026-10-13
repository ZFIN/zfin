// User-facing message for an HTTP 413 on an upload. The 413 may come from a reverse proxy
// (e.g. nginx's client_max_body_size) rather than Tomcat, so name the source to help whoever
// handles the support request.
export const uploadTooLargeMessage = (responseText, totalBytes) => {
    const source = /nginx/i.test(responseText || '') ? 'nginx' : 'the server';
    const sizeMB = (totalBytes / (1024 * 1024)).toFixed(1);
    return `This upload (${sizeMB} MB) exceeds the upload size limit (HTTP 413 Request Entity Too Large from ${source}). ` +
        `Please contact ${process.env.ZFIN_ADMIN} for help.`;
};

// Resolve a fetch() upload response to its JSON body, or reject with an Error whose message is
// suitable for display: the 413 message above, the backend's JSON error message, or the status.
export const parseUploadResponse = async (response, files) => {
    if (response.ok) {
        return response.json();
    }
    const text = await response.text();
    if (response.status === 413) {
        const totalBytes = files.reduce((sum, file) => sum + file.size, 0);
        throw new Error(uploadTooLargeMessage(text, totalBytes));
    }
    let message;
    try {
        message = JSON.parse(text).message;
    } catch {
        // not a JSON error body (e.g. an HTML error page)
    }
    throw new Error(message || `Upload failed (HTTP ${response.status} ${response.statusText})`);
};
