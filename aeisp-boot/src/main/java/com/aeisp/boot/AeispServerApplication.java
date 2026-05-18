package com.aeisp.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AEISP Server 启动类。
 *
 * <p>扫描 com.aeisp 包及其子包下的所有 Spring 组件。</p>
 *
 * @author AEISP Team
 */
@EnableScheduling
@MapperScan("com.aeisp.**.mapper")
@SpringBootApplication(scanBasePackages = "com.aeisp")
public class AeispServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AeispServerApplication.class, args);
    }
}
