package com.company.server.config;

import com.company.server.handlers.JlmqHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;

@Component
@Configuration
@ComponentScan(basePackages = "com.company")
@PropertySource("classpath:application.properties")
@EnableAspectJAutoProxy
public class ApplicationContextConfig {
    @Autowired
    private Environment environment;

    @Bean
    public WebSocketHandler jlmqHandler() {
        return new JlmqHandler();
    }

}
