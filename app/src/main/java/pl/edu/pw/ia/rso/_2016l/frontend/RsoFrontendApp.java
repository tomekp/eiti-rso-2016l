package pl.edu.pw.ia.rso._2016l.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RsoFrontendApp {

    public static void main(String[] args) {
        SpringApplication.run(RsoFrontendApp.class, args);
    }

}
