import React from 'react';
import PropTypes from 'prop-types';
import useFetch from '../../hooks/useFetch';
import LoadingSpinner from '../LoadingSpinner';
import GenericErrorMessage from '../GenericErrorMessage';

const PubCorrespondenceNeeded = ({pubId}) => {
    const url = '/action/api/correspondence/need/' + pubId;
    const {
        value: correspondenceReasons,
        setValue: setCorrespondenceReasons,
        pending,
        failed,
    } = useFetch(url);
    const [saving, setSaving] = React.useState(false);
    const [errorSaving, setErrorSaving] = React.useState(false);

    const saveToServer = (reasons) => {
        setSaving(true);
        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(reasons)
        }).then(() => {
            setSaving(false);
            setErrorSaving(false);
        }).catch(() => {
            setSaving(false);
            setErrorSaving(true);
        });
    }
    const onToggleReason = (reason, checked) => {
        let updatedReasons = [ ...correspondenceReasons ];
        updatedReasons.find(r => r.id === reason.id).needed = checked;
        setCorrespondenceReasons(updatedReasons);
        saveToServer(updatedReasons);
    }

    if (pending) {
        return <LoadingSpinner/>;
    }

    if (failed || errorSaving || !correspondenceReasons) {
        return <GenericErrorMessage/>;
    }

    return <>
        <div className='section mb-0 row pl-3'>
            <div className='heading col-sm-12 mb-0'>
                Reasons for Correspondence
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
            {correspondenceReasons.map((reason) =>
                <div key={reason.id} className='col-sm-6 custom-control custom-checkbox pb-2 pt-2 border border-top-0 border-left-0 border-right-0'>
                    <input
                        className='custom-control-input'
                        checked={reason.needed}
                        id={'reason-' + reason.id}
                        type='checkbox'
                        onChange={event => onToggleReason(reason, event.target.checked)}
                    />
                    <label
                        className='cursor-pointer font-weight-normal d-block custom-control-label'
                        htmlFor={'reason-' + reason.id}
                    >{reason.name}</label>
                </div>
            )}
        </div>
    </>;
};

PubCorrespondenceNeeded.propTypes = {
    pubId: PropTypes.string.isRequired,
};

export default PubCorrespondenceNeeded;

