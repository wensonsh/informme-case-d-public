package de.dh.informme.doctorsOffice.patient;

import ca.uhn.hl7v2.HL7Exception;
import de.dh.informme.hl7Mock.Hl7MockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private Hl7MockService hl7MockService;

    /**
     * find a patient by its patientId
     *
     * @param patientId the patient's id
     * @return found patient
     */
    public Patient getPatientById(Long patientId) {
        return patientRepository.findByPatientId(patientId).orElse(null);
    }

    /**
     * save a patient
     *
     * @param patient the patient to save
     * @param message HL7 message of the patient
     * @return saved patient
     */
    public Patient savePatient(Patient patient, String message) {
        try {
            hl7MockService.updateMessage(message, patient.getHl7Id());
        } catch (HL7Exception e) {
            return null;
        }
        return patientRepository.save(patient);
    }

    /**
     * save a patient
     *
     * @param patient the patient to save
     * @return saved patient
     */
    public Patient savePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    /**
     * update a patient
     *
     * @param patient the patient to update
     * @return updated patient
     */
    public Patient updatePatient(Patient patient) {
        return patientRepository.saveAndFlush(patient);
    }

    /**
     * update a patient
     *
     * @param patient the patient to update
     * @param message HL7 message of the patient
     * @return updated patient
     */
    public Patient updatePatient(Patient patient, String message) {
        try {
            hl7MockService.updateMessage(message, patient.getHl7Id());
        } catch (HL7Exception e) {
            return null;
        }
        return patientRepository.saveAndFlush(patient);
    }

    /**
     * find a patient by its HL7 id
     *
     * @param hl7Id the patient's HL7 id
     * @return found patient
     */
    public Patient getPatientByHl7Id(String hl7Id) {
        return patientRepository.findByHl7Id(hl7Id).orElse(null);
    }

    /**
     * find a patient by its first name, last name and birthday
     *
     * @param firstName the patient's first name
     * @param lastName the patient's last name
     * @param birthday the patient's birthday
     * @return found patient
     */
    public List<Patient> getPatientByNameAndBirthday(String firstName, String lastName, Date birthday) {
        return patientRepository.findByFirstNameAndLastNameAndBirthday(firstName, lastName, birthday);
    }

    /**
     * get a random existing patient
     *
     * @return random patient
     */
    public Long getRandomPatientId() {
        Patient patient = patientRepository.findRandom();
        if (patient != null) {
            return patient.getPatientId();
        } else {
            return null;
        }
    }

    /**
     * check if there are duplicates of the given patient
     *
     * @param patient the patient to check
     * @return true if there are duplicates, false if not
     */
    public boolean checkForDuplicates(Patient patient) {
        Patient foundPatient = null;
        List<Patient> patients = new ArrayList<>();
        if (patient != null) {
            if (patient.getHl7Id() != null && !patient.getHl7Id().isBlank()) {
                foundPatient = getPatientByHl7Id(patient.getHl7Id());
            }
            if (foundPatient == null) {
                patients = getPatientByNameAndBirthday(patient.getFirstName(), patient.getLastName(), patient.getBirthday());
            }
            if (foundPatient != null) {
                return true;
            } else return !patients.isEmpty();
        }
        return false;
    }

    /**
     * set a random HL7 id for the given patient
     *
     * @return random HL7 id
     */
    public String getRandomNumberString() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }
}
