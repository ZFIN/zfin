import React, { useState } from 'react';
import * as FigureService from '../../api/figure';
import FileInput from '../../utils/file-input';

function FigureUpload({ pubId, figures, hasPermissions, onSave = () => {} }) {
    const [label, setLabel] = useState('');
    const [caption, setCaption] = useState('');
    const [files, setFiles] = useState([]);
    const [uploading, setUploading] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [reRenderFileInput, setReRenderFileInput] = useState('');

    const handleLabelChange = (event) => {
        setLabel(event.target.value);
    };

    const handleCaptionChange = (event) => {
        setCaption(event.target.value);
    };

    const handleFileChange = (files) => {
        setFiles([...files]);
    };

    const upload = () => {
        setUploading(true);
        FigureService.addFigure(pubId, 'Fig. ' + label, caption, files)
            .then((response) => {
                let newFigures = [...figures];
                newFigures.push(response);
                onSave(newFigures);

                setLabel('');
                setCaption('');
                setReRenderFileInput(Math.random());
                setErrorMessage('');
            })
            .catch((response) => {
                if (response && response.message) {
                    setErrorMessage(response.message);
                }
            })
            .finally(() => {
                setUploading(false);
            });
    };

    return (
        <form className='form-horizontal'>
            <div className='form-group row'>
                <label className='col-md-2 col-form-label'>Label</label>
                <div className='col-md-6'>
                    Fig. <input
                        className='form-control form-control-fixed-width-sm'
                        value={label}
                        onChange={handleLabelChange}
                    />
                </div>
            </div>
            {hasPermissions ? (
                <>
                    <div className='form-group row'>
                        <label className='col-md-2 col-form-label'>Caption</label>
                        <div className='col-md-6'>
                            <textarea
                                className='form-control'
                                rows='6'
                                value={caption}
                                onChange={handleCaptionChange}
                            />
                        </div>
                    </div>
                    <div className='form-group row'>
                        <label className='col-md-2 col-form-label'>Images</label>
                        <div className='col-md-6'>
                            <div>
                                <FileInput accept={'image/*'} onChange={handleFileChange} multiple={true} errorMessage={errorMessage} reRenderKey={reRenderFileInput}/>
                            </div>
                        </div>
                    </div>
                </>
            ) : (
                <div className='row'>
                    <div className='offset-md-2 col-md-6'>
                        <div className='alert alert-warning'>
                            Publication&apos;s journal does not grant automatic permission to display captions and images.
                            If this publication has permission, indicate so in the <a href='#details'>Details</a> tab.
                        </div>
                    </div>
                </div>
            )}
            <div className='form-group row'>
                <div className='offset-md-2 col-md-6'>
                    <button
                        className='btn btn-primary'
                        onClick={upload}
                        disabled={!label || uploading}
                    >
                        {!uploading ? 'Save' : <i className='fas fa-spin fa-spinner'/>}
                    </button>
                    <span className='text-danger' style={{ display: errorMessage ? 'block' : 'none' }}>{errorMessage}</span>
                </div>
            </div>
        </form>
    );
}

export default FigureUpload;
