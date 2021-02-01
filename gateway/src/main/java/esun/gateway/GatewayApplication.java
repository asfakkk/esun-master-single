package esun.gateway;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableApolloConfig
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	/**
	 * 分配网关路由
	 * @param builder
	 * @return
	 */
	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder){
		return builder.routes()
				.route("tokenService",r -> r.path("/token/**").uri("lb://token"))
				.route("dbHelper",r ->r.path("/dbHelper/**").uri("lb://dbHelper"))
				.route("wharf",r->r.path("/*/wharf/**").uri("lb://wharf"))
				.route("menuManage",r->r.path("/*/menuManage/**").uri("lb://example"))
				.route("role",r->r.path("/*/roleManage/**").uri("lb://example"))
				.route("example",r->r.path("/*/example/**").uri("lb://example"))
				.route("userManage",r->r.path("/*/userManage/**").uri("lb://example"))
				.route("menudManage",r->r.path("/*/menudManage/**").uri("lb://example"))
				.route("corp",r->r.path("/*/corpManage/**").uri("lb://example"))
				.route("domainManage",r->r.path("/*/domainManage/**").uri("lb://example"))
				.build();

	}

}
