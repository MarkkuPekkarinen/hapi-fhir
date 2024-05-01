package ca.uhn.fhir.rest.server;

import static org.junit.jupiter.api.Assertions.assertNull;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.test.utilities.HttpClientExtension;
import ca.uhn.fhir.test.utilities.server.RestfulServerExtension;
import ca.uhn.fhir.util.TestUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateDstu3Test {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(UpdateDstu3Test.class);
	@RegisterExtension
	private static HttpClientExtension ourClient = new HttpClientExtension();
	private static String ourConditionalUrl;
	private static final FhirContext ourCtx = FhirContext.forDstu3Cached();
	@RegisterExtension
	private static RestfulServerExtension ourServer = new RestfulServerExtension(ourCtx)
		.registerProvider(new PatientProvider());
	private static IdType ourId;
	private static InstantType ourSetLastUpdated;

	@BeforeEach
	public void before() {
		ourConditionalUrl = null;
		ourId = null;
		ourSetLastUpdated = null;
	}

	@Test
	public void testUpdateReturnsETagAndUpdate() throws Exception {

		Patient patient = new Patient();
		patient.setId("123");
		patient.addIdentifier().setValue("002");
		ourSetLastUpdated = new InstantType("2002-04-22T11:22:33.022Z");

		HttpPut httpPost = new HttpPut(ourServer.getBaseUrl() + "/Patient/123");
		httpPost.setEntity(new StringEntity(ourCtx.newXmlParser().encodeResourceToString(patient), ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));

		HttpResponse status = ourClient.execute(httpPost);

		String responseContent = IOUtils.toString(status.getEntity().getContent(), StandardCharsets.UTF_8);
		IOUtils.closeQuietly(status.getEntity().getContent());

		ourLog.info("Response was:\n{}", responseContent);
		ourLog.info("Response was:\n{}", status);

		assertThat(responseContent).isNotEmpty();

		Patient actualPatient = (Patient) ourCtx.newXmlParser().parseResource(responseContent);
		assertThat(actualPatient.getIdElement().getIdPart()).isEqualTo(patient.getIdElement().getIdPart());
		assertThat(actualPatient.getIdentifier().get(0).getValue()).isEqualTo(patient.getIdentifier().get(0).getValue());

		assertThat(status.getStatusLine().getStatusCode()).isEqualTo(200);
		assertNull(status.getFirstHeader("location"));
		assertThat(status.getFirstHeader("content-location").getValue()).isEqualTo(ourServer.getBaseUrl() + "/Patient/123/_history/002");
		assertThat(status.getFirstHeader(Constants.HEADER_ETAG_LC).getValue()).isEqualTo("W/\"002\"");
		assertThat(status.getFirstHeader(Constants.HEADER_LAST_MODIFIED_LOWERCASE).getValue()).isEqualTo("Mon, 22 Apr 2002 11:22:33 GMT");

	}

	@Test
	public void testUpdateConditional() throws Exception {

		Patient patient = new Patient();
		patient.setId("001");
		patient.addIdentifier().setValue("002");

		HttpPut httpPost = new HttpPut(ourServer.getBaseUrl() + "/Patient?_id=001");
		httpPost.setEntity(new StringEntity(ourCtx.newXmlParser().encodeResourceToString(patient), ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));

		CloseableHttpResponse status = ourClient.execute(httpPost);
		try {
			String responseContent = IOUtils.toString(status.getEntity().getContent(), StandardCharsets.UTF_8);
			ourLog.info("Response was:\n{}", responseContent);
			assertThat(status.getStatusLine().getStatusCode()).isEqualTo(200);

			assertThat(ourConditionalUrl).isEqualTo("Patient?_id=001");
			assertNull(ourId);
		} finally {
			IOUtils.closeQuietly(status.getEntity().getContent());
		}

	}

	@Test
	public void testUpdateMissingIdInBody() throws Exception {

		Patient patient = new Patient();
		patient.addIdentifier().setValue("002");

		HttpPut httpPost = new HttpPut(ourServer.getBaseUrl() + "/Patient/001");
		httpPost.setEntity(new StringEntity(ourCtx.newXmlParser().encodeResourceToString(patient), ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));

		HttpResponse status = ourClient.execute(httpPost);

		String responseContent = IOUtils.toString(status.getEntity().getContent(), StandardCharsets.UTF_8);
		IOUtils.closeQuietly(status.getEntity().getContent());

		ourLog.info("Response was:\n{}", responseContent);

		assertThat(status.getStatusLine().getStatusCode()).isEqualTo(400);

		OperationOutcome oo = ourCtx.newXmlParser().parseResource(OperationOutcome.class, responseContent);
		assertThat(oo.getIssue().get(0).getDiagnostics()).isEqualTo(Msg.code(419) + "Can not update resource, resource body must contain an ID element for update (PUT) operation");
	}

	@Test
	public void testUpdateNormal() throws Exception {

		Patient patient = new Patient();
		patient.setId("001");
		patient.addIdentifier().setValue("002");

		HttpPut httpPost = new HttpPut(ourServer.getBaseUrl() + "/Patient/001");
		httpPost.setEntity(new StringEntity(ourCtx.newXmlParser().encodeResourceToString(patient), ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));

		CloseableHttpResponse status = ourClient.execute(httpPost);
		try {
			String responseContent = IOUtils.toString(status.getEntity().getContent(), StandardCharsets.UTF_8);
			ourLog.info("Response was:\n{}", responseContent);
			assertThat(status.getStatusLine().getStatusCode()).isEqualTo(200);

			assertNull(ourConditionalUrl);
			assertThat(ourId.getValue()).isEqualTo("Patient/001");
		} finally {
			IOUtils.closeQuietly(status.getEntity().getContent());
		}

	}

	@Test
	public void testUpdateWrongIdInBody() throws Exception {

		Patient patient = new Patient();
		patient.setId("Patient/3/_history/4");
		patient.addIdentifier().setValue("002");

		HttpPut httpPost = new HttpPut(ourServer.getBaseUrl() + "/Patient/1/_history/2");
		httpPost.setEntity(new StringEntity(ourCtx.newXmlParser().encodeResourceToString(patient), ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));

		HttpResponse status = ourClient.execute(httpPost);

		String responseContent = IOUtils.toString(status.getEntity().getContent(), StandardCharsets.UTF_8);
		IOUtils.closeQuietly(status.getEntity().getContent());

		ourLog.info("Response was:\n{}", responseContent);

		assertThat(status.getStatusLine().getStatusCode()).isEqualTo(400);
		assertThat(responseContent).contains("Resource body ID of &quot;3&quot; does not match");
	}

	public static class PatientProvider implements IResourceProvider {

		@Override
		public Class<Patient> getResourceType() {
			return Patient.class;
		}

		@Update()
		public MethodOutcome updatePatient(@IdParam IdType theId, @ResourceParam Patient thePatient, @ConditionalUrlParam String theConditionalUrl) {
			ourId = theId;
			ourConditionalUrl = theConditionalUrl;
			IdType id = theId != null ? theId.withVersion(thePatient.getIdentifierFirstRep().getValue()) : new IdType("Patient/1");
			OperationOutcome oo = new OperationOutcome();
			oo.addIssue().setDiagnostics("OODETAILS");
			if (id.getValueAsString().contains("CREATE")) {
				return new MethodOutcome(id, oo, true);
			}

			thePatient.getMeta().setLastUpdatedElement(ourSetLastUpdated);

			MethodOutcome retVal = new MethodOutcome(id, oo);
			retVal.setResource(thePatient);
			return retVal;
		}

	}

	@AfterAll
	public static void afterClassClearContext() throws Exception {
		TestUtil.randomizeLocaleAndTimezone();
	}
}
