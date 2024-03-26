function backendBaseUrl() {
    //TODO: This is a hack to get the domain for developing locally.  It should be removed when this is deployed to production.
    let calculatedDomain = window.location.origin;
    if (calculatedDomain.indexOf('localhost') > -1) {
        calculatedDomain = 'https://cell-mac.zfin.org';
    }
    return calculatedDomain;
}

export {backendBaseUrl};