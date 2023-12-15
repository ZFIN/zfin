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

    function submit() {
        console.log('submit');
    }

    function readyToSubmit() {
        return (type === 'text only') || (label !== '');
    }

    const onToggle = () => {
        console.log('onToggle');
        toggle();
    }

    return (
        <div className="quick-fig-content">
            <button type="button" className='close' onClick={() => onToggle()}>×</button>
            <h4>Quick Figure</h4>
            <form className='form-inline'>
                <select className='form-control mr-1' value={type} onChange={e => setType(e.target.value)}>
                    {types.map(t => (
                        <option key={t} value={t}>{t}</option>
                    ))}
                </select>
                <input
                    className='form-control form-control-fixed-width-sm mr-1'
                    type="text"
                    value={label}
                    onChange={e => setLabel(e.target.value)}
                    disabled={type === 'text only'}
                />
                <i className="fas fa-check"></i>
                <button className='btn btn-primary' type="button" onClick={submit} disabled={submitting || !readyToSubmit()}>
                    {submitting ? 'Loading...' : 'Submit'}
                </button>
            </form>
            {successMessage && <p>{successMessage}</p>}
            {errorMessage && <p>{errorMessage}</p>}
        </div>
    );
}

const QuickFigure = ({ pubId }) => {
    const popoverRef = useRef(null);
    const [open, setOpen] = useState(false);
    const [types] = useState(['Fig.', 'text only', 'Table']);
    const [type, setType] = useState(types[0]);
    const [label, setLabel] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [isInitialized, setIsInitialized] = useState(false);

    const popoverTemplate = `<div id="popover-dialog-container"></div>`;

    const toggle = () => {
        $(popoverRef.current).popover('toggle');
    };

    const submit = () => {
        let labelFinal = type;
        if (type !== 'text only') {
            labelFinal += ' ' + label;
        }
        setSubmitting(true);

        // something like: axios.post('/api/figures', { pubId, label: labelFinal })...
        // .then(response => {
        //     setSuccessMessage(response.data.label + " created");
        //     setLabel('');
        //     setErrorMessage('');
        //     // Refresh figures, replace $window.refreshFigures
        // })
        // .catch(error => {
        //     setSuccessMessage('');
        //     setErrorMessage(error.response.data.message);
        // })
        // .finally(() => {
        //     setSubmitting(false);
        // });
    };

    const readyToSubmit = () => {
        return (type === 'text only') || (label !== '');
    };

    const reset = () => {
        setType(types[0]);
        setLabel('');
        setSuccessMessage('');
        setErrorMessage('');
    };

    const handlePopoverInserted = (event) => {
        console.log('handlePopoverInserted event:', event);
        if (!isInitialized) {
            setIsInitialized(true);
            const popoverEl = document.getElementById('popover-dialog-container');
            const dialog = <QuickFigureDialog pubId={pubId} toggle={() => toggle()}/>;
            ReactDOM.render(dialog, popoverEl);
        }
    }

    useEffect(() => {
        console.log('useEffect 001');
        if (popoverRef.current) {
            console.log('useEffect 002');

            const returnValue = $(popoverRef.current).popover({
                trigger: 'manual',
                html: true,
                content: () => popoverTemplate,
                placement: 'bottom',
                sanitize: false
            }).on('inserted.bs.popover', handlePopoverInserted);
            console.log('returnValue', returnValue);
        }

        return () => {
            // Clean up the popover when the component is unmounted
            if (popoverRef.current) {
                $(popoverRef.current).popover('dispose');
            }
        };
    }, []);

    return (
        <span className='quick-figure-add'>
            <a href="#" ref={popoverRef} data-toggle='popover' onClick={toggle}>Add Figure XYZ</a>
        </span>
    );
};


QuickFigure.propTypes = {
    pubId: PropTypes.string,
}

export default QuickFigure;
