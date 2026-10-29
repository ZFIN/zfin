import React, {useState} from 'react';
import PropTypes from 'prop-types';

// Personal flags have three states: ENABLED, DISABLED, and UNSET (no row stored,
// which defers to the global flag).
const OPTIONS = [
    {state: 'ENABLED', value: 'true', icon: 'fa-check', title: 'Enabled', active: 'btn-primary', inactive: 'btn-outline-primary'},
    {state: 'UNSET', value: 'unset', icon: 'fa-minus', title: 'Neutral (use the global setting)', active: 'btn-secondary', inactive: 'btn-outline-secondary'},
    {state: 'DISABLED', value: 'false', icon: 'fa-times', title: 'Disabled', active: 'btn-danger', inactive: 'btn-outline-danger'},
];

const FeatureFlagTriState = ({
    state: initialState,
    name,
    url,
}) => {

    const [state, setState] = useState(initialState);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(false);

    const saveState = (option) => {
        setLoading(true);
        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                name: name,
                value: option.value
            }),
        })
            .then((res) => {
                if (!res.ok) {
                    throw new Error('Save failed');
                }
                return res.json();
            })
            .then(() => {setState(option.state); setLoading(false);})
            .catch(() => {setError(true); setLoading(false);});
    };

    if (error) {
        return <div className={'alert alert-danger'}>Error saving.</div>;
    }

    return <div className={'btn-group'}>
        {OPTIONS.map((option) => {
            const selected = state === option.state;
            return <button
                key={option.state}
                title={option.title}
                disabled={selected || loading}
                onClick={() => saveState(option)}
                className={'btn ' + (selected ? option.active : option.inactive)}
            >
                <i className={'fas ' + option.icon}/>
            </button>;
        })}
    </div>;
};

FeatureFlagTriState.propTypes = {
    state: PropTypes.oneOf(['ENABLED', 'DISABLED', 'UNSET']).isRequired,
    name: PropTypes.string.isRequired,
    url: PropTypes.string.isRequired
};

export default FeatureFlagTriState;
