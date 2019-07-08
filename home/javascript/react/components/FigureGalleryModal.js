import React, {Component} from 'react';
import PropTypes from 'prop-types';

class FigureGalleryModal extends Component {
    constructor(props) {
        super(props);
        this.modalRef = React.createRef();
    }

    componentDidMount() {
        const $modal = $(this.modalRef.current);
        $modal.on('hidden.bs.modal', this.props.onClose)
            .find('.figure-gallery-modal-image').on('load', function () {
                $modal.figureGalleryResize();
            });
    }

    componentDidUpdate() {
        if (this.props.image) {
            $(this.modalRef.current).modal('show');
        }
    }

    handleNavigation(callback) {
        return event => {
            event.preventDefault();
            callback(event);
        }
    }

    render() {
        const { image, onPrev, onNext } = this.props;
        return (
            <div className="modal figure-gallery-modal" tabIndex="-1" role="dialog" ref={this.modalRef}>
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close">
                                <span aria-hidden="true"><i className="fas fa-fw fa-times" /></span>
                            </button>
                            <h4 className="modal-title">{image && image.label}</h4>
                        </div>
                        <div className="modal-body figure-gallery-modal-body">
                            {onPrev && <a href='#' className="figure-gallery-modal-nav prev" role="button" onClick={this.handleNavigation(onPrev)}>
                                <i className="fas fa-chevron-left" />
                            </a>}
                            <img className="figure-gallery-modal-image" src={image && image.fullPath} />
                            {onNext && <a href='#' className="figure-gallery-modal-nav next" role="button" onClick={this.handleNavigation(onNext)}>
                                <i className="fas fa-chevron-right" />
                            </a>}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

FigureGalleryModal.propTypes = {
    image: PropTypes.object,
    onPrev: PropTypes.func,
    onNext: PropTypes.func,
    onClose: PropTypes.func,
};

export default FigureGalleryModal;