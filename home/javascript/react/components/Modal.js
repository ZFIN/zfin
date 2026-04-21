import { useEffect, useState } from 'react';
import { createPortal } from 'react-dom';
import PropTypes from 'prop-types';

const Modal = ({ children, open = false, onClose, config = {} }) => {
    // Create a stable container element that serves as both the portal target
    // and the element jQuery Modal operates on. React attaches event listeners
    // to portal containers, so when jQuery moves this element into its blocker
    // overlay, React's synthetic events (onClick, onSubmit, etc.) still work.
    // This is necessary because React 18 attaches events to the root container
    // instead of document, so elements moved outside the root by jQuery would
    // otherwise lose all React event handling.
    const [container] = useState(() => {
        const el = document.createElement('div');
        el.className = 'jq-modal';
        return el;
    });
    const [jqModal, setJqModal] = useState(null);

    // Mount container to DOM and initialize jQuery modal reference
    useEffect(() => {
        document.body.appendChild(container);
        const $modal = $(container);
        setJqModal($modal);
        $modal.on($.modal.AFTER_CLOSE, onClose);
        return () => {
            if (container.parentNode) {
                container.parentNode.removeChild(container);
            }
        };
    }, []);

    // Open/close the jQuery modal
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
    }, [open, jqModal]);

    return createPortal(children, container);
};

Modal.propTypes = {
    children: PropTypes.node,
    open: PropTypes.bool,
    onClose: PropTypes.func,
    config: PropTypes.object,
};

export default Modal;
