package com.manish.springgatewaykotlin

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.gateway.handler.predicate.RoutePredicates.host
import org.springframework.cloud.gateway.handler.predicate.RoutePredicates.path
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.context.annotation.Bean
import org.springframework.cloud.gateway.filter.factory.GatewayFilters.hystrix
import org.springframework.cloud.gateway.filter.factory.GatewayFilters.rewritePath
import org.springframework.cloud.gateway.route.Routes
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory
import org.springframework.security.config.web.server.HttpSecurity
import org.springframework.security.core.userdetails.MapUserDetailsRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.server.SecurityWebFilterChain


@SpringBootApplication
class SpringGatewayKotlinApplication

fun main(args: Array<String>) {
    print("Hello World")
    SpringApplication.run(SpringGatewayKotlinApplication::class.java, *args)
}

@Bean
fun customRouteLocator(rateLimiter: RequestRateLimiterGatewayFilterFactory): RouteLocator {
    return Routes.locator()
            .route("path_route")
            .predicate(path("/get"))
            .uri("http://httpbin.org:80")
            .route("host_route")
            .predicate(host("*.myhost.org"))
            .uri("http://httpbin.org:80")
            .route("rewrite_route")
            .predicate(host("*.rewrite.org"))
            .filter(rewritePath("/foo/(?<segment>.*)", "/\${segment}"))
            .uri("http://httpbin.org:80")
            .route("hystrix_route")
            .predicate(host("*.hystrix.org"))
            .filter(hystrix("slowcmd"))
            .uri("http://httpbin.org:80")
            .route("limit_route")
            .predicate(host("*.limited.org").and(path("/anything/**")))
            .filter(rateLimiter.apply(1, 2))
            .uri("http://httpbin.org:80")
            .route("websocket_route")
            .predicate(path("/echo"))
            .uri("ws://localhost:9000")
            .build()
}

@Bean
@Throws(Exception::class)
fun springWebFilterChain(http: HttpSecurity): SecurityWebFilterChain {
    return http.httpBasic().and()
            .authorizeExchange()
            .pathMatchers("/anything/**").authenticated()
            .anyExchange().permitAll()
            .and()
            .build()
}

@Bean
fun userDetailsRepository(): MapUserDetailsRepository {
    val user = User.withUsername("user").password("password").roles("USER").build()
    return MapUserDetailsRepository(user)
}
