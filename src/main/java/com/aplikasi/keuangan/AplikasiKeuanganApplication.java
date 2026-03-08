package com.aplikasi.keuangan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AplikasiKeuanganApplication {

	public static void main(String[] args) {
		SpringApplication.run(AplikasiKeuanganApplication.class, args);
	}

}
