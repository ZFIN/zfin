import React, {Component} from 'react';
import PropTypes from 'prop-types';

class FigureGalleryModal extends Component {
    constructor(props) {
        super(props);
        this.modalRef = React.createRef();
        this.state = {
            imageLoading: false,
        }
    }

    componentDidMount() {
        const $modal = $(this.modalRef.current);
        $modal.on('hidden.bs.modal', this.props.onClose)
            .find('.figure-gallery-modal-image').on('load', () => {
                this.setState({imageLoading: false});
                $modal.figureGalleryResize();
            });
    }

    componentDidUpdate(prevProps) {
        const $modal = $(this.modalRef.current);
        const {image, imageDetails} = this.props;
        if (image) {
            $modal.bootstrapModal('show');
            if (!prevProps.image || image.url !== prevProps.image.url) {
                this.setState({imageLoading: true});
            }
        }
        if (imageDetails !== prevProps.imageDetails && !this.state.imageLoading) {
            $modal.figureGalleryResize();
        }
    }

    handleNavigation(callback) {
        return event => {
            event.preventDefault();
            callback(event);
        }
    }

    render() {
        const { image, imageDetails, onPrev, onNext } = this.props;
        return (
            <div className='modal figure-gallery-modal' tabIndex='-1' role='dialog' ref={this.modalRef}>
                <div className='modal-dialog'>
                    <div className='modal-content'>
                        <div className='modal-header'>
                            <div>
                                {image && (
                                    <h4 className='modal-title'>
                                        <a href={`/${image.figure.zdbID}`}>{image.figure.label}</a> {image.figure.publication && (
                                            <React.Fragment>
                                                from <a href={`/${image.figure.publication.zdbID}`} dangerouslySetInnerHTML={{__html: image.figure.publication.shortAuthorList}} />
                                            </React.Fragment>)}
                                    </h4>
                                )}
                                <div className='figure-gallery-modal-details'>
                                    {imageDetails}
                                </div>
                            </div>
                            <button type='button' className='close' data-dismiss='modal' aria-label='Close'>
                                <span aria-hidden='true'><i className='fas fa-fw fa-times' /></span>
                            </button>
                        </div>
                        <div className='modal-body figure-gallery-modal-body'>
                            {onPrev && <a href='#' className='figure-gallery-modal-nav prev' role='button' onClick={this.handleNavigation(onPrev)}>
                                <i className='fas fa-chevron-left' />
                            </a>}
                            <a href={image && `/${image.figure.zdbID}`}>
                                <img className='figure-gallery-modal-image' src={image && image.url} />
                            </a>
                            {onNext && <a href='#' className='figure-gallery-modal-nav next' role='button' onClick={this.handleNavigation(onNext)}>
                                <i className='fas fa-chevron-right' />
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
    imageDetails: PropTypes.node,
    onPrev: PropTypes.func,
    onNext: PropTypes.func,
    onClose: PropTypes.func,
};

export default FigureGalleryModal;