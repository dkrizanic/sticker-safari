package com.stickersafari;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class StickerSafariApplication {

    public static void main(String[] args) {
        SpringApplication.run(StickerSafariApplication.class, args);
    }

}
