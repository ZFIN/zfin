export const getFigures = (pubId) => {
    return fetch(`/action/publication/${pubId}/figures`)
        .then(response => response.json());
};

export const addFigure = (pubId, label, caption, fileList) => {
    const form = new FormData();
    form.append('label', label);
    form.append('caption', caption);
    fileList.forEach(file => {
        form.append('files', file);
    });

    return fetch(`/action/publication/${pubId}/figures`, {
        method: 'POST',
        body: form
    }).then(response => response.json());
};

export const updateFigure = async (fig) => {
    const response = await fetch(`/action/figure/${fig.zdbId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(fig),
    });
    return response.json();
};

export const deleteFigure = (fig) => {
    return fetch(`/action/figure/${fig.zdbId}`, {
        method: 'DELETE'
    });
};

export const addImage = (fig, file) => {
    const form = new FormData();
    form.append('file', file);
    return fetch(`/action/figure/${fig.zdbId}/images`, {
        method: 'POST',
        body: form
    }).then(response => response.json());
};

export const deleteImage = (img) => {
    return fetch(`/action/image/${img.zdbId}`, {
        method: 'DELETE'
    });
};

