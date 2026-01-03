package kz.yandex.clientshop.config;

import kz.yandex.clientshop.client.ApiClient;
import kz.yandex.clientshop.client.api.PaymentsApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient paymentWebClient(PaymentAppProperties properties,
        ReactiveOAuth2AuthorizedClientManager authorizedClientManager
    ) {
            ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                    new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

            oauth2Client.setDefaultClientRegistrationId("keycloak");

            return WebClient.builder()
                    .baseUrl(getBaseUrl(properties))
                    .filter(oauth2Client)
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
