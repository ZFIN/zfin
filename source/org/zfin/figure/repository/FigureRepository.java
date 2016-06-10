package org.zfin.figure.repository;

import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.figure.presentation.ExpressionTableRow;
import org.zfin.marker.Clone;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.List;

public interface FigureRepository {

    public Figure getFigure(String zdbID);
    public Figure getDeepFetchedFigure(String zdbID);


    public List<Person> getSubmitters(Publication publication, Clone probe);

    public List<Figure> getFiguresForDirectSubmissionPublication(Publication publication, Clone probe);

    public List<Image> getImages(List<String> imageIDs);
}