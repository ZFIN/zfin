import React from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import LoadingSpinner from '../components/LoadingSpinner';
import GenericErrorMessage from '../components/GenericErrorMessage';
import FeatureToggle from './FeatureToggle';

const FeatureToggles = ({
    url,
}) => {

    const featureFlags = useFetch(url);

    if (featureFlags.pending) {
        return <LoadingSpinner/>;
    }

    if (featureFlags.failed || !featureFlags.value) {
        return <GenericErrorMessage/>;
    }

    return <table className='table col-sm-6'>
        <thead>
            <tr>
                <th>Flag</th>
                <th>Current State</th>
            </tr>
        </thead>
        <tbody>
            {featureFlags.value.map((flag) =>
                <tr key={flag.name}>
                    <td>Use {flag.name}</td>
                    <td><FeatureToggle name={flag.name} enabled={flag.enabled} url={url}/></td>
                </tr>
            )}
        </tbody>
    </table>

};

FeatureToggles.propTypes = {
    url: PropTypes.string.isRequired
};

export default FeatureToggles;
