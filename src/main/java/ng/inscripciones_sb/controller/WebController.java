package ng.inscripciones_sb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {
    @RequestMapping(value = "/{path:^(?!api).*$}")
    public String redirect() {
        return "forward:/index.html";
    }
}
