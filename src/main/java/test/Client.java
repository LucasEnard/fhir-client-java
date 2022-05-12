package test;

import java.util.ArrayList;

import org.apache.http.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicHeader;

import org.hl7.fhir.instance.model.api.IIdType;

import org.hl7.fhir.r4.model.* ;
import org.hl7.fhir.r4.model.ContactPoint.* ;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class Client {

   /**
    * This is the Java main method, which gets executed
    */
   public static void main(String[] args) {

      //----------------------------------------------------------------------------------------------------------
      // Part 1

      // Create a context usign FHIR R4
      FhirContext ctx = FhirContext.forR4();

      // create an header containing the api key for the httpClient
      Header header = new BasicHeader("x-api-key", "sVgCTspDTM4iHGn51K5JsaXAwJNmHkSG3ehxindk");
      ArrayList<Header> headers = new ArrayList<Header>();
      headers.add(header);

      // create an httpClient builder and add the header to it
      HttpClientBuilder builder = HttpClientBuilder.create();
      builder.setDefaultHeaders(headers);

      // create an httpClient using the builder
      CloseableHttpClient httpClient = builder.build();

      // Set the httpClient to the context using the factory
      ctx.getRestfulClientFactory().setHttpClient(httpClient);

      // Create a client
      IGenericClient client = ctx.newRestfulGenericClient("https://fhir.8ty581k3dgzj.static-test-account.isccloud.io");

      //----------------------------------------------------------------------------------------------------------
      // Part 2

      // Create a patient and add a name to it
      Patient patient = new Patient();
      patient.addName()
         .setFamily("FamilyName")
         .addGiven("GivenName1")
         .addGiven("GivenName2");

      // See also patient.setGender or setBirthDateElement

      // Create the resource patient on the server
		MethodOutcome outcome = client.create()
         .resource(patient)
         .execute();

      // Log the ID that the server assigned
      IIdType id = outcome.getId();
      System.out.println("");
      System.out.println("Created patient, got ID: " + id);
      System.out.println("");

      
      //----------------------------------------------------------------------------------------------------------
      // Part 3

      // Search for a single patient with the exact family name "FamilyName" and the exact given name "GivenName1"
      patient = (Patient) client.search()
         .forResource(Patient.class)
         .where(Patient.FAMILY.matchesExactly().value("FamilyNameTest"))
         .and(Patient.GIVEN.matchesExactly().value("GivenName1Test"))
         .returnBundle(Bundle.class)
         .execute()
         .getEntryFirstRep()
         .getResource();

      // Create a telecom for patient
      patient.addTelecom()
         .setSystem(ContactPointSystem.PHONE)
         .setUse(ContactPointUse.HOME)
         .setValue("555-555-5555");

      // Change the patient given name to another
      patient.getName().get(0).getGiven().set(0,  new StringType("AnotherGivenNameTest"));

      // Update the resource patient on the server
      MethodOutcome outcome2 = client.update()
         .resource(patient)
         .execute();

      //----------------------------------------------------------------------------------------------------------
      // Part 4

      // Create a CodeableConcept and fill it
      CodeableConcept codeableConcept = new CodeableConcept();
      codeableConcept.addCoding()
         .setSystem("http://snomed.info/sct")
         .setCode("1234")
         .setDisplay("CodeableConceptDisplay");

      // Create a Quantity and fill it
      Quantity quantity = new Quantity();
      quantity.setValue(1.0);
      quantity.setUnit("kg");

      // Create a Category and fill it
      CodeableConcept category = new CodeableConcept();
      category.addCoding()
         .setSystem("http://snomed.info/sct")
         .setCode("1234")
         .setDisplay("CategoryDisplay");

      // Create a list of CodeableConcepts and put category into it
      ArrayList<CodeableConcept> codeableConcepts = new ArrayList<CodeableConcept>();
      codeableConcepts.add(category);

      // Create an Observation
      Observation observation = new Observation();
      observation.setStatus(Observation.ObservationStatus.FINAL);
      observation.setCode(codeableConcept);
      observation.setSubject(new Reference().setReference("Patient/" + ((IIdType) outcome2.getId()).getIdPart()));
      observation.setCategory(codeableConcepts);
      observation.setValue(quantity);
      
      System.out.println("");
      System.out.println("Created observation, reference : " + observation.getSubject().getReference());
      System.out.println("");

       // Create the resource observation on the server
      MethodOutcome outcome3 = client.create()
         .resource(observation)
         .execute();

      // Print the response of the server
      System.out.println("");
      System.out.println("Created observation, got ID: " + outcome3.getId());
      System.out.println("");

   }
}
