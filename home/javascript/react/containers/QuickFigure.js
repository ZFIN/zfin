import React, {useEffect, useState, useRef} from 'react';
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';

const QuickFigureDialog = ({pubId, toggle}) => {
    const [types] = useState(['Fig.', 'text only', 'Table']);
    const [type, setType] = useState(types[0]);
    const [label, setLabel] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [submitting, setSubmitting] = useState(false);

    const submit = (event) => {
        event.preventDefault();
        let labelFinal = type;
        if (type !== 'text only') {
            labelFinal += ' ' + label;
        }
        setSubmitting(true);

        const url = `/action/publication/${pubId}/figures`;
        const formData = new FormData();
        formData.append('label', labelFinal);
        formData.append('caption', '');

        // add empty array of image files since the server expects it
        //formData.append('files', new Blob([], { type: 'image/*' }));

        fetch(url, {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => { throw err; });
                }
                return response.json();
            })
            .then(data => {
                setSuccessMessage(data.label + ' created');
                setLabel('');
                setErrorMessage('');
                window.refreshFigures();
            })
            .catch(error => {
                setSuccessMessage('');
                setErrorMessage(error.message || 'An error occurred');
            })
            .finally(() => {
                setSubmitting(false);
            });
    };

    function readyToSubmit() {
        return (type === 'text only') || (label !== '');
    }

    return (
        <div className='quick-fig-content'>
            <button type='button' className='close' onClick={() => toggle()}>×</button>
            <h4>Quick Figure</h4>
            <form className='form-inline' onSubmit={submit}>
                <select className='form-control mr-1' value={type} onChange={e => setType(e.target.value)}>
                    {types.map(t => (
                        <option key={t} value={t}>{t}</option>
                    ))}
                </select>
                <input
                    className='form-control form-control-fixed-width-sm mr-1'
                    type='text'
                    value={label}
                    onChange={e => setLabel(e.target.value)}
                    disabled={type === 'text only'}
                />

                <button className='btn btn-primary' type='button' onClick={submit} disabled={submitting || !readyToSubmit()}>
                    {submitting ? <i className='fas fa-spinner fa-spin'/> : <i className='fas fa-check'/>}
                </button>
            </form>
            <p/>
            {successMessage && <p className='text-success'>{successMessage}</p>}
            {errorMessage && <p className='text-danger'>{errorMessage}</p>}
            <small>
                <a href={`/action/publication/${pubId}/edit#figures`}>Add figure with images and caption</a>
            </small>
        </div>
    );
};

const QuickFigure = ({ pubId }) => {
    const popoverRef = useRef(null);
    const [isInitialized, setIsInitialized] = useState(false);
    const popoverTemplate = '<div id=\'popover-dialog-container\'></div>';

    const toggle = () => {
        $(popoverRef.current).popover('toggle');
    };

    const handlePopoverInserted = () => {
        if (!isInitialized) {
            setIsInitialized(true);
            const popoverEl = document.getElementById('popover-dialog-container');
            const dialog = <QuickFigureDialog pubId={pubId} toggle={() => toggle()}/>;
            ReactDOM.render(dialog, popoverEl);
        }
    }

    //set up the popover
    useEffect(() => {
        if (popoverRef.current) {
            $(popoverRef.current).popover({
                trigger: 'manual',
                html: true,
                content: () => popoverTemplate,
                placement: 'bottom',
                sanitize: false
            }).on('inserted.bs.popover', handlePopoverInserted);
        }

        return () => {
            // Clean up the popover when the component is unmounted
            if (popoverRef.current) {
                $(popoverRef.current).popover('dispose');
            }
        };
    }, []);

    return (
        <span className='quick-figure-add quick-fig'>
            <a href='#' className='small-new-link' ref={popoverRef} data-toggle='popover' onClick={toggle}>Add Figure</a>
        </span>
    );
};


QuickFigure.propTypes = {
    pubId: PropTypes.string,
}

export default QuickFigure;
