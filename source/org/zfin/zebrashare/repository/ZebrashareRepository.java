package org.zfin.zebrashare.repository;

import org.zfin.feature.Feature;
import org.zfin.profile.Lab;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.zebrashare.FeatureCommunityContribution;
import org.zfin.zebrashare.ZebrashareEditor;
import org.zfin.zebrashare.ZebrashareSubmissionMetadata;

import java.util.List;

public interface ZebrashareRepository {

    ZebrashareSubmissionMetadata addZebrashareSubmissionMetadata(
            Publication publication,
            Person submitterUser,
            Lab labOfOrigin,
            String submitterName,
            String submitterEmail
    );

    ZebrashareSubmissionMetadata getZebraShareSubmissionMetadataForPublication(Publication publication);

    List<ZebrashareEditor> getZebraShareEditorsForPublication(Publication publication);

    Publication getZebraSharePublicationForFeature(Feature feature);

    FeatureCommunityContribution getLatestCommunityContribution(Feature feature);

    boolean isAuthorizedSubmitter(Feature feature, Person person);

}
