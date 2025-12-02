package ng.inscripciones_sb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping({"/", "/{path:.*}"}) // Rutas no API/Archivos
    public String forwardIndex() {
        return "forward:/index.html";
    }
}
