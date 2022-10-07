package be.vlaanderen.informatievlaanderen.ldes.client.exceptions;

public class UnparseableFragmentException extends RuntimeException {

	/** Implements Serializable. */
	private static final long serialVersionUID = 2959837411139399356L;

	private final String fragmentId;

	public UnparseableFragmentException(String fragmentId, Throwable cause) {
		super(cause);
		this.fragmentId = fragmentId;
	}

	@Override
	public String getMessage() {
		return "LdesClient cannot parse fragment id: " + fragmentId;
	}
}
