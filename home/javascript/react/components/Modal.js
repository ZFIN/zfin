import React, { useEffect, useCallback, useState } from 'react';
import PropTypes from 'prop-types';

const Modal = ({ children, open = false, onClose, config={} }) => {
    const [jqModal, setJqModal] = useState(null);
    const modalRefCallback = useCallback(node => {
        if (!node) {
            return;
        }
        const $modal = $(node);
        setJqModal($modal);
        $modal.on($.modal.AFTER_CLOSE, onClose);
    }, []);

    useEffect(() => {
        if (!jqModal) {
            return;
        }
        if (open) {
            jqModal.modal({
                escapeClose: config.escapeClose === undefined ? false : config.escapeClose,
                clickClose: config.clickClose === undefined ? false : config.clickClose,
                showClose: config.showClose === undefined ? false : config.showClose,
                fadeDuration: 100,
            });
        } else {
            $.modal.close();
        }
    }, [open]);

    return (
        <div className='jq-modal' ref={modalRefCallback}>
            {children}
        </div>
    );
};

Modal.propTypes = {
    children: PropTypes.node,
    open: PropTypes.bool,
    onClose: PropTypes.func,
    config: PropTypes.object,
};

export default Modal;
