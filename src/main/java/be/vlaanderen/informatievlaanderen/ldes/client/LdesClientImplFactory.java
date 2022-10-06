package be.vlaanderen.informatievlaanderen.ldes.client;

import org.apache.jena.riot.Lang;

import be.vlaanderen.informatievlaanderen.ldes.client.services.LdesFragmentFetcher;
import be.vlaanderen.informatievlaanderen.ldes.client.services.LdesFragmentFetcherImpl;
import be.vlaanderen.informatievlaanderen.ldes.client.services.LdesService;
import be.vlaanderen.informatievlaanderen.ldes.client.services.LdesServiceImpl;

public class LdesClientImplFactory {

	private LdesClientImplFactory() {
	}

	public static LdesService getLdesService(Lang dataSourceFormat) {
		return new LdesServiceImpl(dataSourceFormat);
	}

	public static LdesService getLdesService(Lang dataSourceFormat, Long expirationInterval) {
		return new LdesServiceImpl(dataSourceFormat, expirationInterval);
	}

	public static LdesFragmentFetcher getFragmentFetcher(Lang dataSourceFormat) {
		return new LdesFragmentFetcherImpl(dataSourceFormat);
	}
}
