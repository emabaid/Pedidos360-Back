package abaid.pedidos360.bff.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convierte el token JWT de Microsoft Entra ID en un Authentication de Spring
 * Security, tomando los App Roles del claim "roles" (Entra ID los entrega asi
 * cuando estan configurados como App Roles en la App Registration de la API)
 * y transformandolos en authorities con prefijo ROLE_, tal como las espera
 * @PreAuthorize("hasRole('ADMINISTRADOR')").
 */
public class EntraRolesConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            roles = List.of();
        }

        // El claim "roles" de Entra ID ya trae el valor tal como se configuró
        // en el App Role (ej. "ROLE_ADMINISTRADOR"), así que se usa tal cual
        // como authority — NO hay que volver a anteponerle "ROLE_", o
        // quedaría "ROLE_ROLE_ADMINISTRADOR" y hasRole('ADMINISTRADOR') nunca
        // haría match.
        Collection<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
