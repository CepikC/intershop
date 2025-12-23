package kz.yandex.clientshop.config;

import kz.yandex.clientshop.client.ApiClient;
import kz.yandex.clientshop.client.api.PaymentsApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient paymentWebClient(PaymentAppProperties properties) {
        return WebClient.builder()
                .baseUrl(getBaseUrl(properties))
                .build();
    }

    @Bean
    public ApiClient paymentApiClient(WebClient paymentWebClient,
                                      PaymentAppProperties properties) {
        ApiClient apiClient = new ApiClient(paymentWebClient);
        apiClient.setBasePath(getBaseUrl(properties));
        return apiClient;
    }

    @Bean
    public PaymentsApi paymentsApi(ApiClient paymentApiClient) {
        return new PaymentsApi(paymentApiClient);
    }


    private String getBaseUrl(PaymentAppProperties properties) {
        return "http://" + properties.getHost() + ":" + properties.getPort();
    }
}
