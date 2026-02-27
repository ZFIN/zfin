package org.zfin.figure.repository;

import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.marker.Clone;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;

public interface FigureRepository {

    Figure getFigure(String zdbID);

    List<Person> getSubmitters(Publication publication, Clone probe);

    List<Figure> getFiguresForDirectSubmissionPublication(Publication publication, Clone probe);

    List<Image> getImages(List<String> imageIDs);

    List<Figure> getAllFigures();

    List<Image> getAllImagesWithFigures();

    List<Image> getRecentlyCuratedImages();

    Set<String> getFigureIdsWithData(List<String> figureIds);
}