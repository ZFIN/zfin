import React, {useState} from 'react';
import PropTypes from 'prop-types';

const FeatureFlag = ({
    enabled,
    name,
    url,
}) => {

    const [state, setState] = useState(enabled);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(false);

    const saveState = (enabled) => {
        setLoading(true);
        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                name: name,
                value: enabled
            }),
        })
            .then((res) => res.json())
            .then(() => setLoading(false))
            .catch(() => {setError(true); setLoading(false);})
    }

    const toggleState = () => {
        saveState(!state);
        setState(!state);
    }

    if (error) {
        return <>
            <div className={'alert alert-danger'}>Error saving.</div>
        </>;
    } else {
        return <>
            {state ?
                <>
                    <button disabled className={'btn btn-primary mr-1'}><i className={'fas fa-check'}/></button>
                    <button onClick={toggleState} className={'btn btn-outline-danger'} disabled={loading}><i className={'fas fa-times'}/></button>
                </>
                :
                <>
                    <button onClick={toggleState} className={'btn btn-outline-primary mr-1'} disabled={loading}><i className={'fas fa-check'}/></button>
                    <button disabled className={'btn btn-danger'}><i className={'fas fa-times'}/></button>
                </>
            }
        </>
    }
};

FeatureFlag.propTypes = {
    enabled: PropTypes.bool.isRequired,
    name: PropTypes.string.isRequired,
    url: PropTypes.string.isRequired
};

export default FeatureFlag;
