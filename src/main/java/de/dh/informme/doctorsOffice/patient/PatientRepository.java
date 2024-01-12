package de.dh.informme.doctorsOffice.patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * find a patient by its patientId
     *
     * @param patientId the patient's id
     * @return found patient
     */
    Optional<Patient> findByPatientId(Long patientId);

    /**
     * find a patient by its HL7 id
     *
     * @param hl7Id the patient's HL7 id
     * @return found patient
     */
    Optional<Patient> findByHl7Id(String hl7Id);

    /**
     * find a patient by its first name, last name and birthday
     *
     * @param firstName the patient's first name
     * @param lastName the patient's last name
     * @param birthday the patient's birthday
     * @return found patient
     */
    List<Patient> findByFirstNameAndLastNameAndBirthday(String firstName, String lastName, Date birthday);

    /**
     * find a random Patient object
     *
     * @return random Patient object
     */
    @Query(value = "SELECT * FROM patients ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Patient findRandom();
}
