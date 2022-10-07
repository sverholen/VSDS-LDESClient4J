package be.vlaanderen.informatievlaanderen.ldes.client;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.vlaanderen.informatievlaanderen.ldes.client.exceptions.LdesException;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesFragment;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesProcessingState;

public class LdesStateManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(LdesStateManager.class);

	private final Long fragmentExpirationInterval;

	protected final Queue<String> fragmentsToProcess;
	protected final List<String> fragmentsInProgress;

	protected final List<String> processedImmutableFragments;
	protected final Map<String, LocalDateTime> processedMutableFragments;
	/**
	 * A map of key-value pairs with the fragment id as key.
	 */
	protected final Map<String, Set<String>> processedMutableFragmentMembers;

	public LdesStateManager(Long fragmentExpirationInterval) {
		this.fragmentExpirationInterval = fragmentExpirationInterval;

		fragmentsToProcess = new ArrayDeque<>();
		fragmentsInProgress = new ArrayList<>();

		processedImmutableFragments = new ArrayList<>();
		processedMutableFragments = new HashMap<>();
		processedMutableFragmentMembers = new HashMap<>();
	}

	public Long getFragmentExpirationInterval() {
		return fragmentExpirationInterval;
	}

	public boolean hasNext() {
		return hasNextQueuedFragment() || nextExpiredFragment() != null;
	}

	public boolean hasNextQueuedFragment() {
		return !fragmentsToProcess.isEmpty();
	}

	/**
	 * Return the next queued fragment or the next expired or expiration-date-less
	 * mutable fragment).
	 *
	 * If there are more fragments queued, return the next one.
	 * If the next mutable fragment has no expiration date set, return it.
	 * If it has an expiration date, return it only if the fragment has expired.
	 *
	 * @return the fragment id (URL) of the next fragment
	 */
	public String next() {
		if (hasNextQueuedFragment()) {
			String fragment = fragmentsToProcess.poll();

			LOGGER.info("NEXT FRAGMENT: queued fragment {}", fragment);
			fragmentsInProgress.add(fragment);
			return fragment;
		}

		String fragment = nextExpiredFragment();
		if (fragment != null && !fragmentsInProgress.contains(fragment)) {
			LOGGER.info("NEXT FRAGMENT: expired fragment {}", fragment);
			fragmentsInProgress.add(fragment);
			return fragment;
		}

		LOGGER.info("NEXT FRAGMENT: none");

		return null;
	}

	public String nextExpiredFragment() {
		for (Map.Entry<String, LocalDateTime> entry : processedMutableFragments.entrySet()) {
			LocalDateTime expirationDate = entry.getValue();

			// If no expiration date is set, assume mutable expired fragment
			if (expirationDate == null) {
				return entry.getKey();
			}

			// If an expiration date is set, only return this mutable fragment if it is
			// expired
			if (expirationDate.isBefore(LocalDateTime.now())) {
				return entry.getKey();
			}
		}

		return null;
	}

	/**
	 * Returns true if the fragment was queued, false otherwise.
	 *
	 * @param fragmentId
	 *            the id of the fragment to queue
	 * @return true if the fragment was queued, false otherwise
	 * @throws LdesException
	 *             if the fragment is not in the queue, not in the processed
	 *             immutable fragments list and present in the processed mutable
	 *             fragments list.
	 */
	public boolean queueFragment(String fragmentId) {
		return queueFragment(fragmentId, null);
	}

	/**
	 * Returns true if the fragment was queued, false otherwise.
	 *
	 * 1) If the fragments queue already contains the fragment id, don't queue it
	 * and return false.
	 * 2) If the fragment is immutable and was already processed, don't queue it and
	 * return false.
	 * 3) If the fragment is not in the processed mutable fragments queue, queue it
	 * and return true.
	 * 4) Throw an {@link LdesException}
	 *
	 * @param fragmentId
	 *            the id of the fragment to queue
	 * @param expirationDate
	 *            the expiration date of the mutable fragment
	 * @return true if the fragment was queued, false otherwise
	 * @throws LdesException
	 *             if the fragment is not in the queue, not in the processed
	 *             immutable fragments list and present in the processed mutable
	 *             fragments list.
	 */
	public boolean queueFragment(String fragmentId, LocalDateTime expirationDate) {
		if (fragmentsToProcess.contains(fragmentId)) {
			LOGGER.info("QUEUE: Not queueing already queued fragment {}", fragmentId);
			return false;
		}

		if (processedImmutableFragments.contains(fragmentId)) {
			LOGGER.info("QUEUE: Not queueing processed immutable fragment {}", fragmentId);
			return false;
		}

		if (!processedMutableFragments.containsKey(fragmentId)) {
			fragmentsToProcess.add(fragmentId);
			processedMutableFragmentMembers.put(fragmentId, new HashSet<>());
			LOGGER.info("QUEUE: Queued fragment {}", fragmentId);
			return true;
		}

		throw new LdesException("Unable to decide if fragment " + fragmentId + " needs to be queued !");
	}

	public void processedFragment(LdesFragment fragment) {
		if (fragment.isImmutable()) {
			processedImmutableFragments.add(fragment.getFragmentId());
			processedMutableFragments.remove(fragment.getFragmentId());
			processedMutableFragmentMembers.remove(fragment.getFragmentId());

			fragment.setState(LdesProcessingState.PROCESSED_IMMUTABLE);

			LOGGER.info("PROCESSED IMMUTABLE FRAGMENT {}", fragment.getFragmentId());
		} else {
			processedMutableFragments.put(fragment.getFragmentId(), Optional.ofNullable(fragment.getExpirationDate())
					.orElse(LocalDateTime.now().plusSeconds(fragmentExpirationInterval)));

			fragment.setState(LdesProcessingState.PROCESSED_MUTABLE);

			LOGGER.info("PROCESSED MUTABLE FRAGMENT {}", fragment.getFragmentId());
		}

		fragmentsToProcess.remove(fragment.getFragmentId());
		fragmentsInProgress.remove(fragment.getFragmentId());
	}

	public boolean shouldProcessMember(LdesFragment fragment, String memberId) {
		if (processedMutableFragmentMembers.containsKey(fragment.getFragmentId())) {
			return processedMutableFragmentMembers.get(fragment.getFragmentId()).add(memberId);
		}

		return false;
	}

	public long countQueuedFragments() {
		return fragmentsToProcess.size();
	}

	public boolean isQueuedFragment(String fragmentId) {
		return fragmentsToProcess.contains(fragmentId);
	}

	public long countProcessedImmutableFragments() {
		return processedImmutableFragments.size();
	}

	public boolean isProcessedImmutableFragment(String fragmentId) {
		return processedImmutableFragments.contains(fragmentId);
	}

	public long countProcessedMutableFragments() {
		return processedMutableFragments.size();
	}

	public boolean isProcessedMutableFragment(String fragmentId) {
		return processedMutableFragments.containsKey(fragmentId);
	}

	public long countProcessedMutableFragmentMembers() {
		return processedMutableFragmentMembers.values().stream().collect(Collectors.counting());
	}

	public void clearAllQueues() {
		fragmentsToProcess.clear();
		processedImmutableFragments.clear();
		processedMutableFragments.clear();
		processedMutableFragmentMembers.clear();
	}
}
