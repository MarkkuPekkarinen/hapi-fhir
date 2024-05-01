package ca.uhn.fhir.jpa.model.entity;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import ca.uhn.fhir.jpa.model.config.PartitionSettings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceIndexedSearchParamUriTest {

	@Test
	public void testHashFunctions() {
		ResourceIndexedSearchParamUri token = new ResourceIndexedSearchParamUri(new PartitionSettings(), "Patient", "NAME", "http://example.com");
		token.setResource(new ResourceTable().setResourceType("Patient"));
		token.calculateHashes();

		// Make sure our hashing function gives consistent results
		assertThat(token.getHashUri().longValue()).isEqualTo(-6132951326739875838L);
	}

	@Test
	public void testEquals() {
		ResourceIndexedSearchParamUri val1 = new ResourceIndexedSearchParamUri()
			.setUri("http://foo");
		val1.setPartitionSettings(new PartitionSettings());
		val1.calculateHashes();
		ResourceIndexedSearchParamUri val2 = new ResourceIndexedSearchParamUri()
			.setUri("http://foo");
		val2.setPartitionSettings(new PartitionSettings());
		val2.calculateHashes();
		assertThat(val1).isNotNull().isEqualTo(val1);
		assertThat(val2).isEqualTo(val1);
		assertThat("").isNotEqualTo(val1);
	}


}
