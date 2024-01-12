package de.dh.informme.hl7Mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
@RequestMapping("/hl7-mock")
public class Hl7MockController {

    @Autowired
    private Hl7MockService hl7MockService;

    /**
     * get request at "/hl7-mock/random"
     *
     * @return ResponseEntity with a random HL7 message
     */
    @GetMapping("/get-random-message")
    public ResponseEntity<?> randomizeHl7Message() {
        return ResponseEntity.ok().body(hl7MockService.getRandomHl7Mock().getMessage());
    }

    /**
     * get request at "/hl7-mock/{id}"
     *
     * @param id   id of the searched HL7 message
     * @return ResponseEntity with a HL7 message or with an error message
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable(value = "id") String id) {
        Hl7Mock hl7Mock = hl7MockService.getByMshId(id);
        if (hl7Mock != null) {
            return ResponseEntity.ok().body(hl7Mock.getMessage());
        } else {
            return ResponseEntity.badRequest().body("Patient existiert nicht.");
        }
    }
}
