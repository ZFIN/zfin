const getSelectedTermQueryParams = (selected) => {
    let queryParams = {};
    if (selected) {
        if (selected.group.type !== 'GlobalAll') {
            queryParams.termId = selected.group.id;
        }
        if (selected.group.type === 'Other') {
            queryParams.isOther = true;
        }
    }
    return queryParams;
};

const ribbonGroupHasData = (ribbonData, group) => {
    if (!ribbonData) {
        return false;
    }
    if (group.type === 'GlobalAll') {
        return ribbonData.subjects[0].nb_annotations > 0;
    }
    let groupId = group.id;
    if (group.type === 'Other') {
        groupId += '-other';
    }
    return ribbonData.subjects[0].groups[groupId] &&
        ribbonData.subjects[0].groups[groupId].ALL.nb_annotations > 0;
}

export {
    getSelectedTermQueryParams,
    ribbonGroupHasData,
};