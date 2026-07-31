const validateUpdateSequenceInfo = (updateSequenceInfo) => {
    if (!updateSequenceInfo) {
        return {};
    }
    const validationErrors = {};
    if (!updateSequenceInfo.accession) {
        validationErrors.accession = 'Accession cannot be empty.';
    }
    if (updateSequenceInfo.references.length === 0) {
        validationErrors.references = 'Reference cannot be empty.';
    }
    if (updateSequenceInfo.length && isNaN(updateSequenceInfo.length)) {
        validationErrors.length = 'Invalid number';
    }
    if ( isGenBank(updateSequenceInfo) && !updateSequenceInfo.accession.charAt(0).match(/[a-z]/i) ) {
        validationErrors.accession = 'GenBank acc starts with letter';
    }
    return validationErrors;
}

const validateNewSequenceInfo = (sequenceInfo) => {
    if (!sequenceInfo) {
        return {};
    }
    const validationErrors = {};
    if (!sequenceInfo.database) {
        validationErrors.database = 'Database cannot be empty.';
    }
    if (!sequenceInfo.accession) {
        validationErrors.accession = 'Accession cannot be empty.';
    }
    if (!sequenceInfo.reference) {
        validationErrors.reference = 'Reference cannot be empty.';
    }
    if (sequenceInfo.length && isNaN(sequenceInfo.length)) {
        validationErrors.length = 'Invalid number';
    }
    if ( isGenBank(sequenceInfo) && !sequenceInfo.accession.charAt(0).match(/[a-z]/i) ) {
        validationErrors.accession = 'GenBank acc starts with letter';
    }
    return validationErrors;
};


function isGenBank(sequenceInfo) {
    if (!sequenceInfo.accession) {
        return false;
    }
    if (sequenceInfo.referenceDatabaseZdbID === 'ZDB-FDBCONT-040412-37') {
        return true;
    }
    if (sequenceInfo.referenceDatabaseZdbID === 'ZDB-FDBCONT-040412-36') {
        return true;
    }
    return false;
}

export {validateUpdateSequenceInfo, validateNewSequenceInfo};