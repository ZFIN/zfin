package org.zfin.gbrowse.presentation

import org.zfin.AbstractZfinIntegrationSpec
import org.zfin.gbrowse.GBrowseTrack
import org.zfin.mapping.GenomeLocation
import org.zfin.properties.ZfinPropertiesEnum
import org.zfin.repository.RepositoryFactory
import spock.lang.Unroll


class GBrowseImageSpec extends AbstractZfinIntegrationSpec {

    def "urls contain correct base url"() {
        when:
        def image = GBrowseImage.builder().build();

        then:
        image.imageUrl.contains(ZfinPropertiesEnum.GBROWSE_IMG_PATH_FROM_ROOT.toString())
        image.linkUrl.contains(ZfinPropertiesEnum.GBROWSE_PATH_FROM_ROOT.toString())
    }

    def "landmark as string"() {
        when:
        def image = GBrowseImage.builder()
                .landmark("sox10")
                .build();

        then:
        image.imageUrl.contains("name=sox10")
        image.linkUrl.contains("name=sox10")
    }

    @Unroll
    def  "landmark set to marker #zdbId"() {
        when:
        def m = RepositoryFactory.markerRepository.getMarkerByID(zdbId)
        def image = GBrowseImage.builder()
                .landmark(m)
                .build()

        then:
        image.imageUrl =~ /\d+%3A\d+..\d+/

        where:
        zdbId << ["ZDB-GENE-011207-1", "ZDB-MRPHLNO-051220-4"]
    }

    @Unroll
    def  "landmark set to feature #zdbId"() {
        when:
        def f = RepositoryFactory.featureRepository.getFeatureByID(zdbId)
        def image = GBrowseImage.builder()
                .landmark(f)
                .build()

        then:
        image.imageUrl =~ /\d+%3A\d+..\d+/

        where:
        zdbId << ["ZDB-ALT-120130-650"]
    }

    def "add tracks"() {
        when:
        def image = GBrowseImage.builder()
                .landmark("runx1")
                .tracks(GBrowseTrack.GENES, GBrowseTrack.TRANSCRIPTS)
                .build()

        then:
        image.imageUrl.contains("type=genes+mRNA")
    }

    def "use default tracks"() {
        when:
        def m = RepositoryFactory.markerRepository.getMarkerByID("ZDB-MRPHLNO-051220-4")
        def image = GBrowseImage.builder()
                .landmark(m)
                .withDefaultTracks()
                .build()

        then:
        image.imageUrl.contains("type=genes+knockdown_reagent+mRNA")
    }

    def "default grid off"() {
        when:
        def image = GBrowseImage.builder().build();

        then:
        image.imageUrl.contains("grid=0")
    }

    def "enable grid"() {
        when:
        def image = GBrowseImage.builder().grid(true).build()

        then:
        image.imageUrl.contains("grid=1")
    }

    def "use default highlighting"() {
        when:
        def m = RepositoryFactory.markerRepository.getMarkerByID("ZDB-GENE-011207-1")
        def image = GBrowseImage.builder()
                .landmark(m)
                .highlight()
                .build()

        then:
        image.imageUrl.contains("h_feat=sox10")
    }

    def "highlight parameter should be lowercased"() {
        when:
        def feature = "la010630Tg"
        def image = GBrowseImage.builder()
                .landmark(feature)
                .highlight()
                .build()

        then:
        image.imageUrl.contains("h_feat=" + feature.toLowerCase())
    }

    def "highlight feature"() {
        when:
        def f = RepositoryFactory.featureRepository.getFeatureByID("ZDB-ALT-120130-650")
        def image = GBrowseImage.builder()
                .landmark("zfand5a")
                .highlight(f)
                .build()

        then:
        image.imageUrl.contains("h_feat=" + f.abbreviation.toLowerCase())
    }

    def "highlight color"() {
        when:
        def image = GBrowseImage.builder()
                .landmark("sox19a")
                .highlight()
                .highlightColor("pink")
                .build()

        then:
        image.imageUrl.contains("h_feat=sox19a%40pink")
    }

    def "marker with padding"() {
        when:
        def m = RepositoryFactory.markerRepository.getMarkerByID("ZDB-GENE-011207-1")
        def location = RepositoryFactory.linkageRepository.getGenomeLocation(m, GenomeLocation.Source.ZFIN)[0]
        def padding = 1000
        def image = GBrowseImage.builder()
                .landmark(m)
                .withPadding(padding)
                .build()

        then:
        image.imageUrl.contains((location.start - padding) + ".." + (location.end + padding))
    }

    def "relative padding"() {
        when:
        def m = RepositoryFactory.markerRepository.getMarkerByID("ZDB-GENE-011207-1")
        def location = RepositoryFactory.linkageRepository.getGenomeLocation(m, GenomeLocation.Source.ZFIN)[0]
        def padding = 0.1
        def absolutePadding = (int) (padding * (location.end - location.start))
        def image = GBrowseImage.builder()
                .landmark(m)
                .withPadding(padding)
                .build()

        then:
        image.imageUrl.contains((location.start - absolutePadding) + ".." + (location.end + absolutePadding))
    }

}
