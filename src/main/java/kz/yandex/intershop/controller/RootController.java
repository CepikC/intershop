package kz.yandex.intershop.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping("/")
    public String redirectToMain() {
        return "redirect:/main/items";
    }
}
