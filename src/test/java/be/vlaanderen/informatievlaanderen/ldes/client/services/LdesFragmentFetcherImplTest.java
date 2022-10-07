package be.vlaanderen.informatievlaanderen.ldes.client.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import be.vlaanderen.informatievlaanderen.ldes.client.LdesClientImplFactory;
import be.vlaanderen.informatievlaanderen.ldes.client.exceptions.UnparseableFragmentException;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesFragment;

@WireMockTest(httpPort = LdesFragmentFetcherImplTest.HTTP_PORT)
class LdesFragmentFetcherImplTest {

	public static final int HTTP_PORT = 10101;

	private final String initialFragmentUrl = "http://localhost:" + HTTP_PORT + "/exampleData";
	private final String actualFragmentUrl = "http://localhost:" + HTTP_PORT
			+ "/exampleData?generatedAtTime=2022-05-05T00:00:00.000Z";
	private final String invalidFragmentUrl = "http://localhost:" + HTTP_PORT + "/invalid_format";

	private LdesService ldesService;

	@Test
	void whenFragmentUrlRedirects_thenFragmentIdWillBeSetToTargetUrl() {
		ldesService = LdesClientImplFactory.getLdesService(Lang.JSONLD11);

		ldesService.queueFragment(initialFragmentUrl);

		LdesFragment fragment = ldesService.processNextFragment();

		assertEquals(actualFragmentUrl, fragment.getFragmentId());
	}

	@Test
	void whenExceptionOccursWhileFetchingFragment_thenUnparseableFragmentExceptionIsThrown() {
		LdesFragmentFetcher fetcher = LdesClientImplFactory.getFragmentFetcher(Lang.NQUADS);

		assertThrows(UnparseableFragmentException.class,
				() -> fetcher.fetchFragment(invalidFragmentUrl));
	}
}
