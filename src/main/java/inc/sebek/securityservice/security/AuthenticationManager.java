package inc.sebek.securityservice.security;

import inc.sebek.securityservice.entity.Roles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {
    private JWTUtil jwtUtil;

    public AuthenticationManager(final JWTUtil jwtUtil) {this.jwtUtil = jwtUtil;}

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        var authToken = authentication.getCredentials().toString();
        String username=null;
        try {
            username = jwtUtil.getUsernameFromToken(authToken);
        } catch (Exception e) {
            log.warn("Issue with parsing of username. Reason: ", e);
        }
        if (nonNull(username) && jwtUtil.validateToken(authToken)) {
            var roles = jwtUtil.getAllClaimsFromToken(authToken)
                               .get("role", getListOfStringsClass())
                               .stream()
                               .map(Roles::valueOf)
                               .collect(toList());
            var auth = new UsernamePasswordAuthenticationToken(username, null, roles.stream()
                                                                                    .map(Roles::mapToGrantedAuthority)
                                                                                    .collect(toList()));
            return Mono.just(auth);
        } else {
            return Mono.empty();
        }
    }

    private Class<ArrayList<String>> getListOfStringsClass() {
        return (Class<ArrayList<String>>) new ArrayList<String>().getClass();
    }
}
