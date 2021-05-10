import React from 'react';
import PropTypes from 'prop-types';
import AuthorEmailCheckboxList from './AuthorEmailCheckboxList';
import LoadingButton from '../LoadingButton';
import PubCorrespondenceTemplateSelector from './PubCorrespondenceTemplateSelector';

const PubCorrespondenceEmailForm = ({authors, email, onCancel, onUpdate, onComplete, onTemplateSelect, loading}) => {
    if (!email) {
        return null;
    }

    let isValid = email.subject && email.message;
    if (email.outgoing) {
        if (!email.to.length && !email.additionalTo) {
            isValid = false;
        }
    } else {
        if (!email.from.email) {
            isValid = false;
        }
    }

    const handleRegisteredRecipientChange = (recipients) => {
        onUpdate({
            ...email,
            to: recipients,
        });
    };

    const handleAdditionalRecipientChange = (event) => {
        onUpdate({
            ...email,
            additionalTo: event.target.value,
        });
    };

    const handleFromChange = (event) => {
        onUpdate({
            ...email,
            from: {email: event.target.value},
        });
    };

    const handleSubjectChange = (event) => {
        onUpdate({
            ...email,
            subject: event.target.value,
        })
    };

    const handleMessageChange = (event) => {
        onUpdate({
            ...email,
            message: event.target.value,
        });
    };

    return (
        <form className='form-horizontal'>
            <div className='form-group row'>
                <label className='col-md-2 col-form-label'>To</label>
                <div className='col-md-6'>
                    {email.outgoing ?
                        <div>
                            {!email.reply && (
                                <AuthorEmailCheckboxList
                                    id='correspondence-authors'
                                    value={email.to}
                                    authors={authors}
                                    onChange={handleRegisteredRecipientChange}
                                />
                            )}
                            <div>
                                <input className='form-control' placeholder='alice@example.edu' value={email.additionalTo} onChange={handleAdditionalRecipientChange} />
                            </div>
                        </div> :
                        <p className='form-control-plaintext'>{email.to[0].email}</p>
                    }
                </div>
            </div>

            <div className='form-group row'>
                <label className='col-md-2 col-form-label'>From</label>
                <div className='col-md-4'>
                    {email.outgoing ?
                        <p className='form-control-plaintext'>{email.from.email}</p> :
                        <input className='form-control' placeholder='alice@example.edu' value={email.from.email} onChange={handleFromChange} />
                    }
                </div>
            </div>

            <div className='form-group row'>
                <label className='col-md-2 col-form-label'>Subject</label>
                <div className='col-md-6'>
                    <input className='form-control' value={email.subject} onChange={handleSubjectChange} />
                </div>
                <div className='col-md-4'>
                    <PubCorrespondenceTemplateSelector onSelect={onTemplateSelect} />
                </div>
            </div>

            <div className='form-group row'>
                <label className='col-md-2 col-form-label'>Message</label>
                <div className='col-md-6'>
                    <textarea className='form-control' rows='5' value={email.message} onChange={handleMessageChange} />
                </div>
            </div>

            <div className='form-group row'>
                <div className='offset-md-2 col-md-10 horizontal-buttons'>
                    <button type='button' className='btn btn-outline-secondary' onClick={onCancel}>Cancel</button>
                    <LoadingButton className='btn btn-primary' disabled={!isValid || loading} loading={loading} onClick={onComplete}>
                        {email.outgoing ? 'Send' : 'Save'}
                    </LoadingButton>
                </div>
            </div>
        </form>
    );
};

PubCorrespondenceEmailForm.propTypes = {
    authors: PropTypes.array,
    email: PropTypes.object,
    onCancel: PropTypes.func,
    onUpdate: PropTypes.func,
    onComplete: PropTypes.func,
    onTemplateSelect: PropTypes.func,
    loading: PropTypes.bool,
};

export default PubCorrespondenceEmailForm;