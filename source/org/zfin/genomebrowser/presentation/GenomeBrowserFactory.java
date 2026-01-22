package org.zfin.genomebrowser.presentation;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.zfin.jbrowse.presentation.JBrowse2ImageBuilder;

@Setter
@Log4j2
@Component("genomeBrowserFactory")
public class GenomeBrowserFactory {

	public static GenomeBrowserImageBuilder getStaticImageBuilder() {
		GenomeBrowserFactory factory = new GenomeBrowserFactory();
		return factory.getImageBuilder();
	}

	public GenomeBrowserImageBuilder getImageBuilder() {
		return new JBrowse2ImageBuilder();
	}

}
