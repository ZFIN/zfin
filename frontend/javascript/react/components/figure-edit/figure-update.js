import React, { useState } from 'react';
import ImageElement from './image-element';
import InlineEditTextarea from '../../utils/inline-edit-textarea';
import * as FigureService from '../../api/figure';

const FigureUpdate = ({ figure, hasPermissions }) => {
    const [imageError, setImageError] = useState('');
    const [uploading, setUploading] = useState(false);

    const addImage = (file) => {
        setUploading(true);
        FigureService.addImage(figure, file)
            .then((response) => {
                setImageError('');
                // Update the figure with the new image.
                let images = Array.isArray(figure.images) ? figure.images : [];
                images.push(response);
                figure.images = images;
            })
            .catch((error) => {
                if (error.message) {
                    setImageError(error.message);
                }
            })
            .finally(() => {
                setUploading(false);
            });
    };

    const deleteImage = (image) => {
        setUploading(true);
        FigureService.deleteImage(image)
            .then(() => {
                setImageError('');
                figure.images = figure.images.filter((img) => img.zdbId !== image.zdbId);
            })
            .finally(() => {
                setUploading(false);
            });
    };

    const updateFigureCaption = (newCaption) => {
        figure.caption = newCaption;
        FigureService.updateFigure(figure)
            .then(() => {
                setImageError('');
            });
    };

    return (
        <>
            {hasPermissions ? (
                <div>
                    <p className='image-edit-block'>
                        {!figure.images || figure.images.length === 0 ? (
                            <span className='text-muted'>No images yet</span>
                        ) : (
                            figure.images.map((image, index) => (
                                <ImageElement
                                    key={index}
                                    image={image}
                                    deleteImage={deleteImage}
                                />
                            ))
                        )}
                        <input
                            id={`image-add-input-${figure.zdbId}`}
                            type='file'
                            className='image-add-input'
                            accept='image/*'
                            onChange={(e) => addImage(e.target.files[0])}
                        />
                        {!uploading ? (
                            <label className='image-add-label' title='Add image' htmlFor={`image-add-input-${figure.zdbId}`}>
                                +
                            </label>
                        ) : (
                            <span className='image-add-uploading'>
                                <i className='fas fa-spinner fa-spin'/>
                            </span>
                        )}
                    </p>
                    <span className='text-danger'>{imageError}</span>
                    <InlineEditTextarea
                        text={figure.caption}
                        defaultText='Add caption'
                        onSave={updateFigureCaption}
                    />
                </div>
            ) : (
                <div>
                    <span className='text-muted'>
                        Cannot add images and caption without permissions.
                    </span>
                </div>
            )}
        </>
    );
};

export default FigureUpdate;