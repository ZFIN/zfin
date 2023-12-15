import React, {useEffect, useState, useRef} from 'react';
import PropTypes from 'prop-types';


const QuickFigure = ({ pubId }) => {
    const popoverRef = useRef(null);
    const [open, setOpen] = useState(false);
    const [types] = useState(['Fig.', 'text only', 'Table']);
    const [type, setType] = useState(types[0]);
    const [label, setLabel] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');

    const popoverTemplate = `
            <div class="quick-fig-content">
              <div>
                <button type="button" class="close"><span aria-hidden="true">&times;</span></button>
                <h4>Quick Figure</h4>
                <form class="form-inline">
                  <select class="form-control mr-1">
                        <option label="Fig." value="string:Fig." selected="selected">Fig.</option>
                        <option label="text only" value="string:text only">text only</option>
                        <option label="Table" value="string:Table">Table</option>                  
                  </select>
                  <input type="text" class="form-control form-control-fixed-width-sm mr-1">
                  <button class="btn btn-primary">
                    <i ng-show="!vm.submitting" class="fas fa-check"></i>
<!--                    <i ng-show="vm.submitting" class="fas fa-spinner fa-spin"></i>-->
                  </button>
                </form>
                <p class="text-success d-none"></p>
                <p class="text-danger d-none"></p>
                <small><a ng-href="/action/publication/${pubId}/edit#figures">Add figure with images and caption</a></small>
              </div>
            </div>    
    `;

    const toggle = () => {
        setOpen(!open);
        reset();
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

    const handlePopoverInserted = (a,b,c,d) => {
        console.log('handlePopoverInserted', a,b,c,d);
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


    useEffect(() => {
        if(popoverRef.current) {
            console.log("popoverRef.current", popoverRef.current);
            open ? $(popoverRef.current).popover('show') : $(popoverRef.current).popover('hide');
        }
    }, [open]);

    return (
        <span className='quick-figure-add'>
            <a href="#" ref={popoverRef} data-toggle='popover' onClick={toggle}>Add Figure XYZ</a>
            <div id="popover-content" className="d-none">
                <div className="quick-fig-content">
                    <button type="button" onClick={toggle}>×</button>
                    <h4>Quick Figure</h4>
                    <form>
                        <select value={type} onChange={e => setType(e.target.value)}>
                            {types.map(t => (
                                <option key={t} value={t}>{t}</option>
                            ))}
                        </select>
                        <input
                            type="text"
                            value={label}
                            onChange={e => setLabel(e.target.value)}
                            disabled={type === 'text only'}
                        />
                        <button type="button" onClick={submit} disabled={submitting || !readyToSubmit()}>
                            {submitting ? 'Loading...' : 'Submit'}
                        </button>
                    </form>
                    {successMessage && <p>{successMessage}</p>}
                    {errorMessage && <p>{errorMessage}</p>}
                </div>
            </div>
            {open && (<div className="d-none"></div>)}
        </span>
    );
};


QuickFigure.propTypes = {
    pubId: PropTypes.string,
}

export default QuickFigure;
