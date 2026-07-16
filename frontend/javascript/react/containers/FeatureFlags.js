import React from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import LoadingSpinner from '../components/LoadingSpinner';
import GenericErrorMessage from '../components/GenericErrorMessage';
import FeatureFlag from './FeatureFlag';

const FeatureFlags = ({
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
                <th>Enabled for Everyone</th>
                <th>Enabled for Me</th>
                <th>Enabled for Others</th>
            </tr>
        </thead>
        <tbody>
            {featureFlags.value.map((flag) =>
                <tr key={flag.name}>
                    <td>{flag.name}</td>
                    <td><FeatureFlag name={flag.name} enabled={flag.enabledForGlobalScope} url={url + '?scope=global'}/></td>
                    <td><FeatureFlag name={flag.name} enabled={flag.enabledForPersonScope} url={url + '?scope=person'}/></td>
                    <td><a className='btn btn-primary' href={'/action/devtool/feature-flags/per-user?flagname=' + flag.name}>More</a></td>
                </tr>
            )}
        </tbody>
    </table>

};

FeatureFlags.propTypes = {
    url: PropTypes.string.isRequired
};

export default FeatureFlags;
