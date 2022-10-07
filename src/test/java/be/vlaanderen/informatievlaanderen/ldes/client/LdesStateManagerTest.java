package be.vlaanderen.informatievlaanderen.ldes.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.vlaanderen.informatievlaanderen.ldes.client.exceptions.LdesException;
import be.vlaanderen.informatievlaanderen.ldes.client.valueobjects.LdesFragment;

class LdesStateManagerTest {

	public static final int HTTP_PORT = 10101;

	private LdesStateManager stateManager;

	private final LocalDateTime fragmentExpirationDate = LocalDateTime.now().plusSeconds(3600L);

	private final LdesFragment fragmentToProcess = new LdesFragment("localhost:" + HTTP_PORT + "/testData?1",
			fragmentExpirationDate);
	private final LdesFragment nextFragmentToProcess = new LdesFragment("localhost:" + HTTP_PORT + "/testData?2",
			fragmentExpirationDate);

	private final String memberIdToProcess = "localhost:" + HTTP_PORT + "/api/v1/data/10228974/2397";

	@BeforeEach
	public void init() {
		stateManager = new LdesStateManager(3600L);
		stateManager.queueFragment(fragmentToProcess.getFragmentId());
	}

	@Test
	void when_StateManagerIsInitialized_QueueHasOnlyOneItemAndReturnsNullOtherwise() {
		assertTrue(stateManager.hasNext());
		assertEquals(fragmentToProcess.getFragmentId(), stateManager.next());

		assertFalse(stateManager.hasNext());
		assertNull(stateManager.next());
	}

	@Test
	void when_tryingToQueueSameFragmentTwice_FragmentDoesNotGetAddedToQueue() {
		assertTrue(stateManager.hasNext());
		assertEquals(1, stateManager.countQueuedFragments());
		stateManager.queueFragment(fragmentToProcess.getFragmentId());
		assertEquals(1, stateManager.countQueuedFragments());

		String nextFragment = stateManager.next();
		assertEquals(fragmentToProcess.getFragmentId(), nextFragment);
		stateManager.processedFragment(new LdesFragment(nextFragment, null));

		stateManager.queueFragment(nextFragmentToProcess.getFragmentId());

		assertEquals(nextFragmentToProcess.getFragmentId(), stateManager.next());
		assertFalse(stateManager.hasNext());
	}

	@Test
	void when_queueingAndProcessingMultipleFragments_queueIsAsExpected() {
		String nextFragment;

		nextFragment = stateManager.next();
		assertEquals(fragmentToProcess.getFragmentId(), nextFragment);
		stateManager.processedFragment(new LdesFragment(nextFragment, null));

		stateManager.queueFragment(nextFragmentToProcess.getFragmentId());

		nextFragment = stateManager.next();
		assertEquals(nextFragmentToProcess.getFragmentId(), nextFragment);

		assertFalse(stateManager.hasNext());

	}

	@Test
	void when_tryingToProcessAnAlreadyProcessedLdesMember_MemberDoesNotGetProcessed() {
		assertTrue(stateManager.shouldProcessMember(fragmentToProcess, memberIdToProcess));
		assertFalse(stateManager.shouldProcessMember(fragmentToProcess, memberIdToProcess));
	}

	@Test
	void when_parsingImmutableFragment_saveAsProcessedPageWithEmptyExpireDate() {
		fragmentToProcess.setImmutable(true);

		assertEquals(0, stateManager.countProcessedImmutableFragments());
		stateManager.processedFragment(fragmentToProcess);
		assertEquals(1, stateManager.countProcessedImmutableFragments());
	}

	@Test
	void when_afterFirstProcessing_fragmentIsEitherMutableOrImmutable() {
		fragmentToProcess.setImmutable(true);

		stateManager.processedFragment(fragmentToProcess);

		boolean isFragmentProcessed = stateManager.isProcessedImmutableFragment(fragmentToProcess.getFragmentId())
				|| stateManager.isProcessedMutableFragment(fragmentToProcess.getFragmentId());

		assertFalse(stateManager.isQueuedFragment(fragmentToProcess.getFragmentId()));
		assertTrue(isFragmentProcessed);
	}

	@Test
	void when_processedFragmentIsImmutable_isContainedInImmutableFragmentQueue() {
		fragmentToProcess.setImmutable(true);

		stateManager.processedFragment(fragmentToProcess);

		assertFalse(stateManager.isQueuedFragment(fragmentToProcess.getFragmentId()));
		assertTrue(stateManager.isProcessedImmutableFragment(fragmentToProcess.getFragmentId()));
		assertFalse(stateManager.isProcessedMutableFragment(fragmentToProcess.getFragmentId()));
	}

	@Test
	void when_processedFragmentIsMutable_isContainedInMutableFragmentQueue() {
		fragmentToProcess.setImmutable(false);

		stateManager.processedFragment(fragmentToProcess);

		assertFalse(stateManager.isQueuedFragment(fragmentToProcess.getFragmentId()));
		assertFalse(stateManager.isProcessedImmutableFragment(fragmentToProcess.getFragmentId()));
		assertTrue(stateManager.isProcessedMutableFragment(fragmentToProcess.getFragmentId()));
	}

	@Test
	void when_onlyImmutableFragments_QueueRemainsEmpty() {
		fragmentToProcess.setImmutable(true);
		stateManager.processedFragment(fragmentToProcess);

		assertFalse(stateManager.hasNext());
	}

	@Test
	void whenFragmentCantBeQueued_thenAnLdesExceptionIsThrown() {
		String fragmentId = fragmentToProcess.getFragmentId();
		LocalDateTime now = LocalDateTime.now();

		// StateManager already has the fragmentToProcess
		assertFalse(stateManager.queueFragment(fragmentId, now));

		// Now mess it up
		stateManager.fragmentsToProcess.remove(fragmentId);
		stateManager.processedImmutableFragments.add(fragmentId);
		assertFalse(stateManager.queueFragment(fragmentId, now));

		// Remove from processed immutable fragments
		stateManager.processedImmutableFragments.remove(fragmentId);
		assertTrue(stateManager.queueFragment(fragmentId, now));

		// Now try the exception
		stateManager.fragmentsToProcess.remove(fragmentId);
		stateManager.processedMutableFragments.put(fragmentId, now);
		assertThrows(LdesException.class, () -> stateManager.queueFragment(fragmentId, now));
	}
}
