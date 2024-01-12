package de.dh.informme.doctorsOffice.hl7;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v26.datatype.CX;
import ca.uhn.hl7v2.model.v26.segment.PID;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.model.v26.message.ADT_A01;
import de.dh.informme.error.DismatchError;
import de.dh.informme.error.DuplicatePatientError;
import de.dh.informme.doctorsOffice.patient.Patient;
import de.dh.informme.doctorsOffice.patient.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@Service
public class Hl7Service {

    @Autowired
    private PatientService patientService;

    @Autowired
    private Hl7Parser hl7Parser;

    /**
     * process a given HL7 message
     *
     * @param hl7MessageAsString hl7 message
     * @throws HL7Exception is thrown when there was an exception while parsing the message
     */
    public Patient processMessage(String hl7MessageAsString, boolean autoMatch) throws Exception {
        HapiContext context = new DefaultHapiContext();
        Parser parser = context.getPipeParser();
        Message message = parser.parse(hl7MessageAsString);

        Patient patient = new Patient();
        boolean patientFound = false;

        if (message instanceof ADT_A01 adtMessage) {
            PID pid = adtMessage.getPID();

            // get the patientIdentifierList and check if the patient is already registered in the database
            CX[] patientIdList = pid.getPatientIdentifierList();
            for (CX cxId : patientIdList) {
                String id = cxId.getIDNumber().getValue();
                if (patientService.getPatientByHl7Id(id) != null) {
                    patient = patientService.getPatientByHl7Id(id);
                    patientFound = true;
                }
            }
            if (patientFound) {
                HashMap<String, String> dismatches = checkForDismatches(patient, pid);
                if (!dismatches.isEmpty()) {
                    if (autoMatch) {
                        Patient updatedPatient = updatePatientWithMessageContent(patient, pid);
                        return patientService.updatePatient(updatedPatient, hl7MessageAsString);
                    } else {
                        throw new DismatchError("There are dismatches between the patient's data in the database and the data in the HL7 message.");
                    }
                }
                return patient;

            } else {
                patient = updatePatientWithMessageContent(patient, pid);
                String firstName = patient.getFirstName();
                String lastName = patient.getLastName();
                Date birthday = patient.getBirthday();
                setRandomId(patient, pid);
                patientFound = patientService.checkForDuplicates(patient);
                if (patientFound) {
                    if (patientService.getPatientByNameAndBirthday(firstName, lastName, birthday).size() > 1) {
                        throw new DuplicatePatientError("There are more than one instances of the patient with the given name and birthday.");
                    } else {
                        Patient foundPatient = patientService.getPatientByNameAndBirthday(firstName, lastName, birthday).get(0);
                        patient.setPatientId(foundPatient.getPatientId());
                        patientService.updatePatient(patient, hl7MessageAsString);
                        return patientService.getPatientByNameAndBirthday(firstName, lastName, birthday).get(0);
                    }
                } else {
                    patientService.updatePatient(patient, hl7MessageAsString);
                    return patientService.savePatient(patient, hl7MessageAsString);
                }
            }
        } else {
            throw new Exception("Message has wrong format.");
        }
    }

    /**
     * set a random id for the PID-patient identifier list for the given patient
     *
     * @param patient given patient
     * @param pid PID segment of the hl7 message
     * @throws HL7Exception is thrown when there was an exception while parsing the message
     */
    public void setRandomId(Patient patient, PID pid) throws HL7Exception {
        CX newIdentifierForList = pid.insertPatientIdentifierList(pid.getPatientIdentifierListReps());
        String randomId = patientService.getRandomNumberString();
        while (patientService.getPatientByHl7Id(randomId) != null) {
            randomId = patientService.getRandomNumberString();
        }
        // set the new entry for patientIdentifierList
        newIdentifierForList.getIDNumber().setValue(randomId);
        newIdentifierForList.getAssigningAuthority().getNamespaceID().setValue("Digital Health Praxis");
        // also save the new id in own patient object
        patient.setHl7Id(newIdentifierForList.getIDNumber().getValue());
    }

    /**
     * check for dismatches between the patient's data in the database and the data in the HL7 message
     *
     * @param patient   patient from the database
     * @param pid       pid segment of the HL7 message
     * @return hashmap with the dismatches
     * @throws ParseException is thrown when there was an exception while parsing the message
     */
    private HashMap<String, String> checkForDismatches(Patient patient, PID pid) throws ParseException {
        HashMap<String, String> dismatches = new HashMap<>();
        String firstName = hl7Parser.getPatientFirstName(pid);
        String lastName = hl7Parser.getPatientLastName(pid);
        Date birthday = hl7Parser.getPatientBirthday(pid);
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        String birthdayString = formatter.format(birthday);
        String address = hl7Parser.getPatientAddressAsString(pid);
        String administrativeSex = hl7Parser.getPatientAdministrativeSex(pid);
        String telephone = hl7Parser.getPatientTelephoneNumber(pid);
        String mail = hl7Parser.getPatientEmail(pid);

        if (!patient.getFirstName().equals(firstName)) {
            dismatches.put("firstName", "firstName");
        }
        if (!patient.getLastName().equals(lastName)) {
            dismatches.put("lastName", "lastName");
        }
        Date patientBirthday = patient.getBirthday();
        String patientBirthdayString = formatter.format(patientBirthday);
        if (!patientBirthdayString.equals(birthdayString)) {
            dismatches.put("birthday", "birthday");
        }
        if (!patient.getAddress().equals(address)) {
            dismatches.put("address", "address");
        }
        if (!patient.getSex().equals(administrativeSex)) {
            dismatches.put("sex", "sex");
        }
        if (!patient.getTelephone().equals(telephone)) {
            dismatches.put("telephone", "telephone");
        }
        if (!patient.getEmail().equals(mail)) {
            dismatches.put("mail", "mail");
        }
        return dismatches;
    }

    /**
     * update the given patient with the content of the HL7 message
     *
     * @param patient   given patient
     * @param pid       pid segment of the HL7 message
     * @return updated patient
     * @throws ParseException
     */
    private Patient updatePatientWithMessageContent(Patient patient, PID pid) throws ParseException {
        // extract information out of PID segment
        String firstName = hl7Parser.getPatientFirstName(pid);
        String lastName = hl7Parser.getPatientLastName(pid);
        Date birthday = hl7Parser.getPatientBirthday(pid);
        String address = hl7Parser.getPatientAddressAsString(pid);
        String administrativeSex = hl7Parser.getPatientAdministrativeSex(pid);
        String telephone = hl7Parser.getPatientTelephoneNumber(pid);
        String mail = hl7Parser.getPatientEmail(pid);

        // create a new patient object with the card's information
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setBirthday(birthday);
        patient.setAddress(address);
        patient.setSex(administrativeSex);
        patient.setTelephone(telephone);
        patient.setEmail(mail);

        return patient;
    }
}
