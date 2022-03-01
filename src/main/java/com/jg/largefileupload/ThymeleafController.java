package com.jg.largefileupload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ThymeleafController {

    @GetMapping("/")
    public String greeting(final Model model) {
        return "file-upload";
    }
}
