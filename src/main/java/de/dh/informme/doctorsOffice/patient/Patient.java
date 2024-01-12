package de.dh.informme.doctorsOffice.patient;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name="patients")
@AllArgsConstructor
@Data
public class Patient {

    /**
     * unique ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientId;

    /**
     * id of the hl7-message
     */
    private String hl7Id;

    /**
     * first name
     */
    private String firstName;

    /**
     * last name
     */
    private String lastName;

    /**
     * birthday
     */
    @JsonFormat(pattern="dd.MM.yyyy", timezone = "Europe/Berlin")
    private Date birthday;

    /**
     * address
     */
    private String address;

    /**
     * telephone number
     */
    private String telephone;

    /**
     * email address
     */
    private String email;

    /**
     * sex
     */
    private String sex;

    public Patient() {
        patientId = 1L;
    }
}
