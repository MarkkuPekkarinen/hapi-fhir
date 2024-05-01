package ca.uhn.fhir.jpa.subscription.match.matcher.subscriber;

import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionCriteriaParserTest {

	@Test
	public void testSearchExpression() {
		String expression = "Patient?foo=bar";
		SubscriptionCriteriaParser.SubscriptionCriteria criteria = SubscriptionCriteriaParser.parse(expression);
		assertThat(criteria.getType()).isEqualTo(SubscriptionCriteriaParser.TypeEnum.SEARCH_EXPRESSION);
		assertThat(criteria.getCriteria()).isEqualTo(expression);
		assertThat(criteria.getApplicableResourceTypes()).containsExactlyInAnyOrder("Patient");
		assertThat(criteria.toString()).isEqualTo("SubscriptionCriteriaParser.SubscriptionCriteria[type=SEARCH_EXPRESSION,criteria=Patient?foo=bar,applicableResourceTypes=[Patient]]");
	}

	@Test
	public void testTypeExpression() {
		String expression = "Patient";
		SubscriptionCriteriaParser.SubscriptionCriteria criteria = SubscriptionCriteriaParser.parse(expression);
		assertThat(criteria.getType()).isEqualTo(SubscriptionCriteriaParser.TypeEnum.SEARCH_EXPRESSION);
		assertThat(criteria.getCriteria()).isEqualTo(expression);
		assertThat(criteria.getApplicableResourceTypes()).containsExactlyInAnyOrder("Patient");
		assertThat(criteria.toString()).isEqualTo("SubscriptionCriteriaParser.SubscriptionCriteria[type=SEARCH_EXPRESSION,criteria=Patient,applicableResourceTypes=[Patient]]");
	}

	@Test
	public void testStarExpression() {
		String expression = "[*]";
		SubscriptionCriteriaParser.SubscriptionCriteria criteria = SubscriptionCriteriaParser.parse(expression);
		assertThat(criteria.getType()).isEqualTo(SubscriptionCriteriaParser.TypeEnum.STARTYPE_EXPRESSION);
		assertNull(criteria.getCriteria());
		assertNull(criteria.getApplicableResourceTypes());
		assertThat(criteria.toString()).isEqualTo("SubscriptionCriteriaParser.SubscriptionCriteria[type=STARTYPE_EXPRESSION]");
	}

	@Test
	public void testMultitypeExpression() {
		String expression = "[Patient   , Observation]";
		SubscriptionCriteriaParser.SubscriptionCriteria criteria = SubscriptionCriteriaParser.parse(expression);
		assertThat(criteria.getType()).isEqualTo(SubscriptionCriteriaParser.TypeEnum.MULTITYPE_EXPRESSION);
		assertNull(criteria.getCriteria());
		assertThat(criteria.getApplicableResourceTypes()).containsExactlyInAnyOrder("Patient", "Observation");
		assertThat(criteria.toString()).isEqualTo("SubscriptionCriteriaParser.SubscriptionCriteria[type=MULTITYPE_EXPRESSION,applicableResourceTypes=[Observation, Patient]]");
	}

	@Test
	public void testInvalidExpression() {
		assertNull(SubscriptionCriteriaParser.parse("[]"));
		assertNull(SubscriptionCriteriaParser.parse(""));
		assertNull(SubscriptionCriteriaParser.parse(null));
		assertNull(SubscriptionCriteriaParser.parse(" "));
		assertNull(SubscriptionCriteriaParser.parse("#123"));
	}

}
