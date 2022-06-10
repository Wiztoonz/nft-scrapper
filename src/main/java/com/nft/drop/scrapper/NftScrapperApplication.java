package com.nft.drop.scrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class NftScrapperApplication {

	public static void main(String[] args) {
		SpringApplication.run(NftScrapperApplication.class, args);
	}

	@Bean
	public WebClient webClient() {
		final int size = 16 * 1024 * 1024;
		final ExchangeStrategies strategies = ExchangeStrategies.builder()
				.codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
				.build();
		return WebClient.builder()
				.exchangeStrategies(strategies)
				.baseUrl("https://www.nftdropscalendar.com/")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}

}
