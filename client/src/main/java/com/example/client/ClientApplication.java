package com.example.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Bean
	CustomerClient cc(WebClient.Builder httpBuilder) {
		var wc = httpBuilder.baseUrl("http://localhost:8082/").build();
		var htps = HttpServiceProxyFactory.builder().clientAdapter(WebClientAdapter.forClient(wc)).build()
				.createClient(CustomerClient.class);
		return htps;
	}

	@Bean
	ApplicationListener<ApplicationReadyEvent> ready(CustomerClient client) {
		return event -> client.all().subscribe(System.out::println);
	}

}

@Controller
class GraphqlController {
	private final CustomerClient cc;

	GraphqlController(CustomerClient cc) {
		this.cc = cc;
	}

	@QueryMapping
	Flux<Customer> customers() {
		return cc.all();
	};
	
	@BatchMapping
	Map<Customer, Profile> profile(List<Customer> customerList) {
		var m = new HashMap<Customer, Profile>();
		for(var c: customerList)
			m.put(c, new Profile(c.id()));
		return m;
	}

//	@SchemaMapping(typeName = "Customer")
//	Profile profile(Customer c) {
//		return new Profile(c.id());
//	}
}

record Profile(Integer id) {
};

record Customer(Integer id, String name) {

}

interface CustomerClient {

	@GetExchange("/customers")
	Flux<Customer> all();

}