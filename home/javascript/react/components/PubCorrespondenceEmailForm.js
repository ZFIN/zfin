import React from 'react';
import PropTypes from 'prop-types';
import AuthorEmailCheckboxList from "./AuthorEmailCheckboxList";
import LoadingButton from "./LoadingButton";

const PubCorrespondenceEmailForm = ({authors, email, onCancel, onUpdate, onComplete, loading}) => {
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
        <form className="form-horizontal">
            <div className="form-group">
                <label className="col-sm-2 control-label">To</label>
                <div className="col-sm-6">
                    {email.outgoing ?
                        <div>
                            {!email.reply && <AuthorEmailCheckboxList value={email.to} authors={authors} onChange={handleRegisteredRecipientChange} />}
                            <div className={!email.reply && authors.length > 0 ? 'checkbox' : ''}>
                                <input className="form-control" placeholder="alice@example.edu" value={email.additionalTo} onChange={handleAdditionalRecipientChange} />
                            </div>
                        </div> :
                        <p className="form-control-static">{email.to[0].email}</p>
                    }
                </div>
            </div>

            <div className="form-group">
                <label className="col-sm-2 control-label">From</label>
                <div className="col-sm-4">
                    {email.outgoing ?
                        <p className="form-control-static">{email.from.email}</p> :
                        <input className="form-control" placeholder="alice@example.edu" value={email.from.email} onChange={handleFromChange} />
                    }
                </div>
            </div>

            <div className="form-group">
                <label className="col-sm-2 control-label">Subject</label>
                <div className="col-sm-6">
                    <input className="form-control" value={email.subject} onChange={handleSubjectChange} />
                </div>
            </div>

            <div className="form-group">
                <label className="col-sm-2 control-label">Message</label>
                <div className="col-sm-6">
                    <textarea className="form-control" rows="5" value={email.message} onChange={handleMessageChange} />
                </div>
            </div>

            <div className="form-group">
                <div className="col-sm-offset-2 col-sm-10 horizontal-buttons">
                    <button type='button' className="btn btn-default" onClick={onCancel}>Cancel</button>
                    <LoadingButton className="btn btn-primary" disabled={!isValid || loading} loading={loading} onClick={onComplete}>
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
    loading: PropTypes.bool,
};

export default PubCorrespondenceEmailForm;