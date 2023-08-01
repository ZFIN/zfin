import React from 'react';

const ImageElement = ({ image, deleteImage }) => (
    <span className="image-edit-image">
    <img src={image.thumbnailPath} alt="" />
    <span
        className="image-delete-button"
        onClick={() => deleteImage(image)}
        title="Remove image"
    >
      <span className="fa-stack fa-lg">
        <i className="fas fa-circle fa-stack-1x"></i>
        <i className="fas fa-times-circle fa-stack-1x"></i>
      </span>
    </span>
  </span>
);

export default ImageElement;