package ca.uhn.fhir.jpa.mdm.svc;

import ca.uhn.fhir.jpa.mdm.BaseMdmR4Test;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MdmResourceFilteringSvcTest extends BaseMdmR4Test {

	@Autowired
	private MdmResourceFilteringSvc myMdmResourceFilteringSvc;

	@Test
	public void testFilterResourcesWhichHaveNoRelevantAttributes() {
		Patient patient = new Patient();
		patient.setDeceased(new BooleanType(true)); // MDM rules defined do not care about the deceased attribute.

		//SUT
		boolean shouldBeProcessed = myMdmResourceFilteringSvc.shouldBeProcessed(patient);

		assertEquals(false, shouldBeProcessed);
	}

	@Test
	public void testDoNotFilterResourcesWithMdmAttributes() {
		Patient patient = new Patient();
		patient.addIdentifier().setValue("Hey I'm an ID! rules defined in mdm-rules.json care about me!");

		//SUT
		boolean shouldBeProcessed = myMdmResourceFilteringSvc.shouldBeProcessed(patient);

		assertEquals(true, shouldBeProcessed);
	}
}
