package abaid.pedidos360.bff.config;

import abaid.pedidos360.bff.security.EntraRolesConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize("hasRole(...)") en los controladores
public class SecurityConfig {

    // https://login.microsoftonline.com/{tenantId}/v2.0  (App Registration con "accessTokenAcceptedVersion": 2)
    @Value("${pedidos360.entra.issuer-uri}")
    private String issuerUri;

    // Client ID (o Application ID URI) de la App Registration de la API/BFF en Entra ID.
    // Debe coincidir con el "aud" que trae el Access Token.
    @Value("${pedidos360.entra.audience}")
    private String audience;

    @Value("${pedidos360.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/publico").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(new EntraRolesConverter())
                )
            );

        return http.build();
    }

    /**
     * Segunda validacion del JWT (defensa en profundidad): el API Gateway ya
     * valido el token en el borde, pero el BFF lo vuelve a validar por
     * completo (issuer, firma, vigencia y ademas audience, que el decoder
     * por defecto de Spring NO valida).
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        org.springframework.security.oauth2.jwt.NimbusJwtDecoder decoder =
            (org.springframework.security.oauth2.jwt.NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);

        OAuth2TokenValidator<org.springframework.security.oauth2.jwt.Jwt> withIssuer =
            JwtValidators.createDefaultWithIssuer(issuerUri);

        OAuth2TokenValidator<org.springframework.security.oauth2.jwt.Jwt> withAudience =
            jwtToken -> {
                if (jwtToken.getAudience() != null && jwtToken.getAudience().contains(audience)) {
                    return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success();
                }
                return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                    new org.springframework.security.oauth2.core.OAuth2Error(
                        "invalid_token", "El audience del token no coincide con " + audience, null)
                );
            };

        decoder.setJwtValidator(
            new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(List.of(withIssuer, withAudience))
        );

        return decoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(java.util.Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
