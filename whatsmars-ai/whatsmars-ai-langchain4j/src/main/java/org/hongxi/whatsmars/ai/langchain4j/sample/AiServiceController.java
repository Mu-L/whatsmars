package org.hongxi.whatsmars.ai.langchain4j.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiServiceController {

    @Autowired
    private Assistant assistant; // 直接注入声明式接口

    @GetMapping("/ai/chat")
    public String chat(@RequestParam String message) {
        return assistant.chat(message);
    }
}