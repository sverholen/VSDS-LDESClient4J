package be.vlaanderen.informatievlaanderen.ldes.client.services;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;

import be.vlaanderen.informatievlaanderen.ldes.client.LdesStateManager;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesFragment;

public interface LdesService {

	/**
	 * Queues a fragment for immediate fetching.
	 *
	 * Internally calls {@link #queueFragment(String, LocalDateTime)} with an
	 * expirationDate of LocalDateTime.now().
	 *
	 * @param fragmentId
	 *            the fragment id (i.e. URL) to process.
	 */
	void queueFragment(String fragmentId);

	/**
	 * Queues a fragment for processing after the fragments expirationDate.
	 *
	 * @param fragmentId
	 *            the fragment id (i.e. URL) to process.
	 * @param expirationDate
	 *            the fragment's expiration date.
	 */
	void queueFragment(String fragmentId, LocalDateTime expirationDate);

	/**
	 * Checks if there are unprocessed fragments
	 *
	 * @return true if there are unprocessed fragments, false otherwise.
	 */
	boolean hasFragmentsToProcess();

	/**
	 * Processes the next available LDES fragment.
	 *
	 * @return the processed LDES fragment containing the members and an expiration
	 *         date.
	 */
	LdesFragment processNextFragment();

	LdesStateManager getStateManager();

	Stream<Statement> extractRelations(Model fragmentModel);

	/**
	 * Returns the expected {@link Lang} of the data source.
	 *
	 * This value is forced on the jena parsers.
	 *
	 * @return the expected {@link Lang} of the data source
	 */
	Lang getDataSourceFormat();

	/**
	 * Returns the interval that is added to construct a fragment expiration
	 * interval.
	 *
	 * When a fragment is received without an expiration interval, e.g. for HTTP
	 * requests without a max-age element in the Cache-control header, this value is
	 * added to the current time to set the expiration date.
	 *
	 * @return the interval that is added to construct a fragment expiration
	 *         interval
	 */
	Long getFragmentExpirationInterval();
}
