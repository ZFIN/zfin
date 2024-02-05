import React from 'react';
import PropTypes from 'prop-types';

// <style>
//     .copy-attribute-target {
//     display: inline-block;
//     cursor: pointer;
// }
//     .copy-attribute-target:hover {
//     /*text-decoration: underline;*/
// }
//     .copy-attribute-icon {
//     display: none;
//     cursor: pointer;
// }
//     .copy-attribute-target:hover + .copy-attribute-icon {
//     display: inline;
// }
// </style>


const CopyTarget = ({innerHTML}) => {

    console.log('innerHTML', innerHTML);

    const displaySuccessMessage = (element) => {
        // Create the tooltip
        $(element).attr('title', 'Copied').tooltip();

        // Show the tooltip
        $(element).tooltip('show');

        // hide the tooltip after 1 second
        setTimeout(function() {
            $(element).tooltip('dispose');
        }, 1000);
    }

    const copyClickHandler = (event) => {
        navigator.clipboard.writeText(event.target.innerText.trim()).then(() => {
            displaySuccessMessage(event.target);
        }, (err) => {
            //ignore copy to clipboard error
        });
    }

    return (
        <>
            <span onClick={copyClickHandler} dangerouslySetInnerHTML={{__html: innerHTML}} />
            <i className='far fa-copy'/>
        </>
    );
};

CopyTarget.props = {
    innerHTML: PropTypes.node
};

export default CopyTarget;
