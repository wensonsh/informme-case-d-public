package de.dh.informme.error;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("error")
public class ErrorHandlingController {
    @GetMapping({"", "/", "/error"})
    public String handleError() {
        return "error";
    }
}
