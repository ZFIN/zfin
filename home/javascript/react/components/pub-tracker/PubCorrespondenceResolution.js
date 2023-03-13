import React from 'react';
import PropTypes from 'prop-types';
import useFetch from '../../hooks/useFetch';
import GenericErrorMessage from '../GenericErrorMessage';

const PubCorrespondenceResolution = ({pubId}) => {
    const url = '/action/api/correspondence/resolution/' + pubId;
    const {
        value: correspondenceResolutions,
        setValue: setCorrespondenceResolutions,
        pending,
        failed,
    } = useFetch(url);
    const [saving, setSaving] = React.useState(false);
    const [errorSaving, setErrorSaving] = React.useState(false);

    const saveToServer = (resolutions) => {
        setSaving(true);
        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(resolutions)
        }).then(() => {
            setSaving(false);
            setErrorSaving(false);
        }).catch(() => {
            setSaving(false);
            setErrorSaving(true);
        });
    }
    const onToggleResolution = (resolution) => {
        let updatedResolutions = [ ...correspondenceResolutions ];
        updatedResolutions = updatedResolutions.map(r => ({...r, resolved:  r.id === resolution.id}));
        setCorrespondenceResolutions(updatedResolutions);
        saveToServer(updatedResolutions);
    }

    if (pending) {
        return (null);
    }

    if (failed || errorSaving || !correspondenceResolutions) {
        return <GenericErrorMessage/>;
    }

    return <>
        <div className='section mb-0 row pl-3'>
            <div className='heading col-sm-12 mb-0 mt-5'>
                Resolution
            </div>
        </div>
        <div className='row pl-2'>
            <div className='col-sm-12 pl-2'>
                { saving ?
                    <span className='saving-indicator'> <i className='fa fa-spinner fa-spin'/> Saving...</span> :
                    <span className='saving-indicator-placeholder invisible'> No Changes </span>
                }
            </div>
        </div>
        <div className='row pl-2 pr-3'>
            {correspondenceResolutions.map((resolution) =>
                <div key={resolution.id} className='col-sm-3 custom-control custom-radio pb-2 pt-2'>
                    <input
                        className='custom-control-input'
                        checked={resolution.resolved}
                        id={'resolution-' + resolution.id}
                        type='radio'
                        name='correspondence-resolution'
                        onChange={() => onToggleResolution(resolution)}
                    />
                    <label
                        className='cursor-pointer font-weight-normal d-block custom-control-label'
                        htmlFor={'resolution-' + resolution.id}
                    >{resolution.name}</label>
                </div>
            )}
        </div>
    </>;
};

PubCorrespondenceResolution.propTypes = {
    pubId: PropTypes.string.isRequired,
};

export default PubCorrespondenceResolution;

