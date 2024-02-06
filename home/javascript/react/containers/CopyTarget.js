import React, {useState, useEffect, useRef} from 'react';
import PropTypes from 'prop-types';

import './CopyTargetStyle.css';

const CopyTarget = ({innerHTML}) => {
    const [showingTooltip, setShowingTooltip] = useState(false);
    const copyTargetRef = useRef(null);
    const displaySuccessMessage = (element) => {
        // Create the tooltip
        $(element).attr('title', 'Copied').tooltip();

        // Show the tooltip
        $(element).tooltip('show');

        // hide the tooltip after 1 second
        setTimeout(function() {
            setShowingTooltip(false);
            $(element).tooltip('dispose');
        }, 1000);
    }

    const copyClickHandler = async () => {
        await navigator.clipboard.writeText(copyTargetRef.current.innerText.trim());
        setShowingTooltip(true);
    }

    useEffect(() => {
        if (showingTooltip) {
            displaySuccessMessage('.copy-attribute-target');
        }
    }, [showingTooltip]);

    return (
        <>
            <span
                ref={copyTargetRef}
                className='copy-attribute-target'
                onClick={copyClickHandler}
                dangerouslySetInnerHTML={{__html: innerHTML}}
            />
            <i onClick={copyClickHandler} className='far fa-copy copy-attribute-icon'/>
        </>
    );
};

CopyTarget.props = {
    innerHTML: PropTypes.node
};

export default CopyTarget;
