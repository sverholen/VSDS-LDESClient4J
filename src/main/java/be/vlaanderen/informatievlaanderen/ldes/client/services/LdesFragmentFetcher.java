package be.vlaanderen.informatievlaanderen.ldes.client.services;

import org.apache.jena.riot.Lang;

import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesFragment;

public interface LdesFragmentFetcher {

	/**
	 * Returns the expected {@link Lang} of the data source.
	 *
	 * This value is forced on the jena parsers.
	 *
	 * @return the expected {@link Lang} of the data source
	 */
	Lang getDataSourceFormat();

	LdesFragment fetchFragment(String fragmentUrl);
}
