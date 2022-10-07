package be.vlaanderen.informatievlaanderen.ldes.client;

public class LdesClientDefaults {

	private LdesClientDefaults() {
	}

	/** The expected RDF format of the LDES data source */
	public static final String DEFAULT_DATA_SOURCE_FORMAT = "JSONLD11";
	/** The desired RDF format for output */
	public static final String DEFAULT_DATA_DESTINATION_FORMAT = "n-quads";
	/**
	 * The number of seconds to add to the current time before a fragment is
	 * considered expired.
	 *
	 * Only used when the HTTP request that contains the fragment does not have a
	 * max-age element in the Cache-control header.
	 */
	public static final String DEFAULT_FRAGMENT_EXPIRATION_INTERVAL = "604800";
	/**
	 * The amount of time to wait to call the LdesService when the queue has no
	 * mutable fragments left or when the mutable fragments have not yet expired.
	 */
	public static final String DEFAULT_POLLING_INTERVAL = "60";
}
