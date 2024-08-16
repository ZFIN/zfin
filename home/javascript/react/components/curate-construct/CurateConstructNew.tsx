import React, {useState} from 'react';
import CurateConstructForm from './CurateConstructForm';
import {backendBaseUrl} from './DomainInfo';
import {EditConstructFormDTO} from './ConstructTypes';

const calculatedDomain = backendBaseUrl();

interface CurateConstructNewProps {
    publicationId: string;
    onSuccess: (submissionObject: EditConstructFormDTO) => void;
}

const CurateConstructNew = ({publicationId, onSuccess}: CurateConstructNewProps) => {
    const [display, setDisplay] = useState<boolean>(false);
    const [errorMessage, setErrorMessage] = useState<string>('');
    const [successMessage, setSuccessMessage] = useState<string>('');

    function toggleDisplay() {
        setDisplay(!display);
    }

    async function submitForm(submissionObject : EditConstructFormDTO) {
        setSuccessMessage('');
        setErrorMessage('');
        const result = await fetch(`${calculatedDomain}/action/construct/create-and-update`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(submissionObject),
        });
        const bodyJson = await result.json();
        setSuccessMessage(bodyJson.message);
        if (bodyJson.success) {
            onSuccess(submissionObject);
        } else {
            throw new Error('Error saving construct');
        }
    }

    function cancelEdit() {
        toggleDisplay();
    }

    return <>
        <div className={`mb-3 pub-${publicationId}`}>
            <span className='bold'>NEW CONSTRUCT: </span>
            <a onClick={toggleDisplay} style={{textDecoration: 'underline'}}>{display ? 'Hide' : 'Show'}</a>
            {display && <div className='mt-2'>
                <CurateConstructForm
                    publicationId={publicationId}
                    submitButtonLabel='Save'
                    onCancel={cancelEdit}
                    onSubmit={submitForm}
                />
                <div className='mt-2'>
                    {successMessage && <div className='alert alert-success' dangerouslySetInnerHTML={{__html: successMessage}}/>}
                    {errorMessage && <div className='alert alert-danger'>{errorMessage}</div>}
                </div>
            </div>}
        </div>
    </>;
}

export default CurateConstructNew;
