package org.zfin.gbrowse.presentation

import org.zfin.AbstractZfinIntegrationSpec
import org.zfin.gbrowse.GBrowseTrack
import org.zfin.mapping.GenomeLocation
import org.zfin.mapping.repository.LinkageRepository
import org.zfin.properties.ZfinPropertiesEnum
import org.zfin.repository.RepositoryFactory
import spock.lang.Shared
import spock.lang.Unroll


class GBrowseImageSpec extends AbstractZfinIntegrationSpec {

    @Shared
    LinkageRepository linkageRepository = RepositoryFactory.linkageRepository;

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
        def location = linkageRepository.getGenomeLocation(m, GenomeLocation.Source.ZFIN)?.getAt(0)
        def image = GBrowseImage.builder()
                .landmark(location)
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
        def location = linkageRepository.getGenomeLocation(f, GenomeLocation.Source.ZFIN_Zv9)?.getAt(0)
        def builder = GBrowseImage.builder()
                .landmark(location)
        builder.setGenomeBuild(GBrowseImage.GenomeBuild.ZV9)
        def image = builder.build()

        then:
        image.imageUrl.contains('Zv9')
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

    def "highlight parameter should be lowercased"() {
        when:
        def feature = "la010630Tg"
        def image = GBrowseImage.builder()
                .landmark(feature)
                .highlight(feature)
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
                .highlight("sox19a")
                .highlightColor("pink")
                .build()

        then:
        image.imageUrl.contains("h_feat=sox19a%40pink")
    }

    def "marker with padding"() {
        when:
        def m = RepositoryFactory.markerRepository.getMarkerByID("ZDB-GENE-011207-1")
        def location = linkageRepository.getGenomeLocation(m, GenomeLocation.Source.ZFIN)[0]
        def padding = 1000
        def image = GBrowseImage.builder()
                .landmark(location)
                .withPadding(padding)
                .build()

        then:
        image.imageUrl.contains((location.start - padding) + ".." + (location.end + padding))
    }

    def "relative padding"() {
        when:
        def m = RepositoryFactory.markerRepository.getMarkerByID("ZDB-GENE-011207-1")
        def location = linkageRepository.getGenomeLocation(m, GenomeLocation.Source.ZFIN)[0]
        def padding = 0.1
        def absolutePadding = (int) (padding * (location.end - location.start))
        def image = GBrowseImage.builder()
                .landmark(location)
                .withPadding(padding)
                .build()

        then:
        image.imageUrl.contains((location.start - absolutePadding) + ".." + (location.end + absolutePadding))
    }

}
