package com.example.edge2;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class Edge2Application {

	@Bean
	WebClient client(WebClient.Builder builder){
		return builder.baseUrl("http://localhost:8080").build();
	}
	
	@Bean
	CrmClient crmClient (WebClient webClient){
		var proxy = HttpServiceProxyFactory
				.builder((new WebClientAdapter(webClient)))
				.build();
		return proxy.createClient(CrmClient.class);
	}
	
	@Bean
	ApplicationRunner applicationRunner(CrmClient crmClient){
		return args -> crmClient.getCustomers().subscribe(System.out::println);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Edge2Application.class, args);
	}

}

interface CrmClient {
	
	@GetExchange("/customers")
	Flux<Customer> getCustomers();
}

record Customer(Integer id, String name) {}