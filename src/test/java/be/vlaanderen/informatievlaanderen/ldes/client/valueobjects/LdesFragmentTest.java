package be.vlaanderen.informatievlaanderen.ldes.client.valueobjects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import be.vlaanderen.informatievlaanderen.ldes.client.exceptions.LdesMemberNotFoundException;

class LdesFragmentTest {

	private static final String idMember1 = "1";
	private static final String idMember2 = "2";
	private static final String idNonExistantMember = "-1";

	@Test
	void whenMemberIsNotFound_thenLdesMemberNotFoundExceptionIsThrown() {
		LdesFragment fragment = new LdesFragment();

		fragment.addMember(new LdesMember(idMember1, null));
		fragment.addMember(new LdesMember(idMember2, null));

		// To be sure
		assertNotNull(fragment.getMember(idMember1));
		assertNotNull(fragment.getMember(idMember2));

		assertThrows(LdesMemberNotFoundException.class, () -> fragment.getMember(idNonExistantMember));
	}
}
