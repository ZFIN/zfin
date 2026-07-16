import React from 'react';
import PropTypes from 'prop-types';
import useFetch from '../hooks/useFetch';
import LoadingSpinner from '../components/LoadingSpinner';
import GenericErrorMessage from '../components/GenericErrorMessage';
import FeatureFlag from './FeatureFlag';

const FeatureFlagsPerUser = ({
    url,
    flagname
}) => {

    const featureFlagsPerUser = useFetch(url);
    const editFlagsUrl = '/action/devtool/feature-flags'

    if (featureFlagsPerUser.pending) {
        return <LoadingSpinner/>;
    }

    if (featureFlagsPerUser.failed || !featureFlagsPerUser.value) {
        return <GenericErrorMessage/>;
    }

    const allUserNames = Object.keys(featureFlagsPerUser.value);
    const allUserFlags = featureFlagsPerUser.value;

    return <table className='table col-sm-6'>
        <thead>
            <tr><th>Username</th>
                <th>{flagname} Enabled</th>
            </tr>
        </thead>
        <tbody>
            { allUserNames.map( username =>
                <tr key={username}>
                    <td>{username}</td>
                    <td>
                        <FeatureFlag
                            name={flagname}
                            enabled={allUserFlags[username][flagname] === 'ENABLED'}
                            url={editFlagsUrl + '?scope=person&person=' + username}
                        />
                    </td>
                    <td style={{display: 'none'}}>{allUserFlags[username][flagname]}</td>
                </tr>
            )}
        </tbody>
    </table>

};

FeatureFlagsPerUser.propTypes = {
    url: PropTypes.string.isRequired,
    flagname: PropTypes.string.isRequired
};

export default FeatureFlagsPerUser;
