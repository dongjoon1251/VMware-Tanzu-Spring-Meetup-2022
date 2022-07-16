package com.example.edge1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.net.http.HttpHeaders;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;

@SpringBootApplication
public class Edge1Application {

	public static void main(String[] args) {
		SpringApplication.run(Edge1Application.class, args);
	}

	@Bean
	WebClient webClient(WebClient.Builder builder){
		return builder.build();
	}
}

record Profile (Integer id){}

@Controller
class CrmGrapheqlController {
	private final WebClient http;

	public CrmGrapheqlController(WebClient http) {
		this.http = http;
	}
	
	@BatchMapping
	Map<Customer, Profile> profile(List<Customer> customers){
		var map = new HashMap<Customer, Profile>();
		for(var c: customers)
			map.put(c, new Profile(c.id()));
		return map;
	}
	
//	@SchemaMapping(typeName = "Customer")
//	Profile profile(Customer customer){
//		// todo call other network service!
//		return new Profile(customer.id());
//	}
	
	
	@QueryMapping
//	@SchemaMapping(typeName = "Query", field = "customers")
	Flux<Customer> customers () {
		return this.http
				.get()
				.uri("http://localhost:8080/customers")
				.retrieve()
				.bodyToFlux(Customer.class);
	}
}

record Customer (Integer id, String name){ }

@Configuration
class GatewayConfiguration {
	@Bean
	RouteLocator gateway (RouteLocatorBuilder routeLocatorBuilder){
		return routeLocatorBuilder
				.routes()
				.route(
						rs -> rs.path("/proxy")
								.filters(fs -> fs
										.setPath("/customers")
										.addResponseHeader(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
										.retry(10)
								)
								.uri("http://localhost:8080/")
				)
				.build();
	}
}