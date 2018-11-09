package org.zfin.zebrashare.repository;

import org.zfin.profile.Lab;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.zebrashare.ZebrashareSubmissionMetadata;

public interface ZebrashareRepository {

    ZebrashareSubmissionMetadata addZebrashareSubmissionMetadata(
            Publication publication,
            Person submitterUser,
            Lab labOfOrigin,
            String submitterName,
            String submitterEmail
    );

}
