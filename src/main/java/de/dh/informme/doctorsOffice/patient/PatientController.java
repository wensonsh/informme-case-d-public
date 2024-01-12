package de.dh.informme.doctorsOffice.patient;

import ca.uhn.hl7v2.HL7Exception;
import de.dh.informme.error.DismatchError;
import de.dh.informme.error.DuplicatePatientError;
import de.dh.informme.hl7Mock.Hl7MockService;
import de.dh.informme.doctorsOffice.hl7.Hl7Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private Hl7Service hl7Service;

    @Autowired
    private Hl7MockService hl7MockService;

    @Autowired
    private MessageSource messageSource;

    /**
     * get mapping at "patient/{id}"
     *
     * @param id    id of the searched patient
     * @return page with patient information
     */
    @GetMapping("/show/{id}")
    public ModelAndView getPatient(@PathVariable(value = "id") Long id) {
        // define the html page to show when this mapping is called
        ModelAndView modelAndView = new ModelAndView("patient/patient_show_data");
        // retrieve the patient from the database
        Patient patient = patientService.getPatientById(id);
        if (patient != null) {
            // show the page with patient information only if patient is not null (patient was found)
            modelAndView.addObject(patient);
        } else {
            //wenn kein Patient anhand der (hl7) id gefunden werden konnte => neuen Patienten anlegen
            return new ModelAndView("redirect:/index");
        }
        return modelAndView;
    }

    @GetMapping("/show")
    public ModelAndView showPatient() {
        Long randomId = patientService.getRandomPatientId();
        return new ModelAndView("redirect:/patient/show/" + randomId);
    }

    /**
     * get mapping at "patient/{id}"
     *
     * @return page with patient information
     */
    @GetMapping("/random-visitor")
    public ModelAndView processAndShowRandomPatient() {
        // define the html page to show when this mapping is called
        ModelAndView modelAndView = new ModelAndView("patient/patient_show_data");
        // retrieve the patient from the database
        try {
            Patient patient = hl7Service.processMessage(hl7MockService.getRandomHl7Mock().getMessage(), false);
            if (patient != null) {
                // show the page with patient information only if patient is not null (patient was found)
                return modelAndView.addObject(patient);
            }
        } catch (Exception e) {
            return new ModelAndView("redirect:/error");
        }
        return new ModelAndView("redirect:/index");
    }

    /**
     * REST API
     *
     * get patient by id
     * @param id id of the requested patient
     * @return found patient
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getPatientByRestRequest(@PathVariable(value = "id") Long id) {
        // retrieve the patient from the database
        Patient patient = patientService.getPatientById(id);
        if (patient != null) {
            return ResponseEntity.ok().body(patient);
        } else {
            return ResponseEntity.ok().body(messageSource.getMessage("patient.message.notFound", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * REST API
     * update patient with the given id
     *
     * @param id        id of the
     * @param patient   updated patient data
     * @return updated patient
     */
    @PostMapping("/{id}/update")
    public ResponseEntity<?> updatePatient(@PathVariable(value = "id") String id, @RequestBody Patient patient) {
        try {
            Patient foundPatient = patientService.getPatientById(Long.parseLong(id));
            if (foundPatient == null) {
                return ResponseEntity.badRequest().body("Patient not found");
            } else {
                foundPatient.setFirstName(patient.getFirstName());
                foundPatient.setLastName(patient.getLastName());
                foundPatient.setBirthday(patient.getBirthday());
                foundPatient.setAddress(patient.getAddress());
                foundPatient.setSex(patient.getSex());
                foundPatient.setTelephone(patient.getTelephone());
                foundPatient.setEmail(patient.getEmail());
                Patient updatedPatient = patientService.updatePatient(foundPatient);
                return ResponseEntity.ok().body(updatedPatient);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error while processing your request");
        }
    }

    /**
     * REST API
     * read health card
     *
     * @param hl7Message hl7 message to read
     * @return found DB-patient based on health card
     */
    @PostMapping("/read")
    public ResponseEntity<?> readHealthCard(@RequestBody String hl7Message, @RequestParam(value = "autoUpdate", required = false, defaultValue = "true") boolean autoMatch) {
        try {
            Patient patient = hl7Service.processMessage(hl7Message, autoMatch);
            if (patient != null) {
                return ResponseEntity.ok().body(patient);
            } else {
                return ResponseEntity.badRequest().body("Patient not found");
            }
        } catch (HL7Exception e) {
            return ResponseEntity.badRequest().body("Error while parsing the message.");
        } catch (DuplicatePatientError e) {
            return ResponseEntity.ok().body(messageSource.getMessage("patient.message.duplicate", null, LocaleContextHolder.getLocale()));
        } catch (DismatchError e) {
            return ResponseEntity.ok().body(messageSource.getMessage("patient.message.dismatch", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * REST API
     * create a patient with its given data
     *
     * @param patient patient data
     * @return created patient
     */
    @PostMapping("/create-patient")
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
        try {
            Patient savedPatient = patientService.savePatient(patient);
            return ResponseEntity.ok().body(savedPatient);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error while processing your request");
        }
    }
}
