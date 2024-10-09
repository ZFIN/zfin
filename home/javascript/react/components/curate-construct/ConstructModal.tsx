import React from 'react';

const ConstructModal = ({children}) => {
    const modalOverlayCss = {
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center'
    }

    const modalContentCss = {
        backgroundColor: 'white',
        padding: '20px',
        borderRadius: '5px'
    }

    return (
        <div className='modal-overlay' style={modalOverlayCss}>
            <div className='modal-content' style={modalContentCss} onClick={e => e.stopPropagation()}>
                {children}
            </div>
        </div>
    );
};

export default ConstructModal;
