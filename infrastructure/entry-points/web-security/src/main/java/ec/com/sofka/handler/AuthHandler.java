package ec.com.sofka.handler;


import ec.com.sofka.appservice.commands.CreateUserCommand;
import ec.com.sofka.appservice.commands.usecases.CreateUserUseCase;
import ec.com.sofka.data.AuthRequest;
import ec.com.sofka.data.AuthResponse;
import ec.com.sofka.data.CreateUserRequest;
import ec.com.sofka.enums.ROLE;
import ec.com.sofka.services.JWTService;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AuthHandler {
    private final CreateUserUseCase createUserUseCase;
    private final ReactiveAuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;



    public AuthHandler(CreateUserUseCase createUserUseCase, ReactiveAuthenticationManager authenticationManager, JWTService jwtService, PasswordEncoder passwordEncoder) {
        this.createUserUseCase = createUserUseCase;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<CreateUserRequest> createUser(CreateUserRequest userReqDTO) {
        return createUserUseCase.execute(
                new CreateUserCommand(
                        userReqDTO.getUsername(),
                        passwordEncoder.encode(userReqDTO.getPassword()),
                        userReqDTO.getRoles()
                )
        ).map(res -> new CreateUserRequest(res.getUsername(), res.getPassword(), res.getRoles()));
    }

    public Mono<AuthResponse> authenticate(AuthRequest request) {
        return authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()))
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                .map(auth -> {
                    var userDetails = (UserDetails) auth.getPrincipal();
                    return getAuthResponse(userDetails);
                });
    }

    private AuthResponse getAuthResponse(UserDetails userDetails) {
        var extraClaims = extractAuthorities("roles", userDetails);

        var jwtToken = jwtService.generateToken(userDetails, extraClaims);
        return new AuthResponse(jwtToken);
    }

    private Map<String, Object> extractAuthorities(String key, UserDetails userDetails) {
        Map<String, Object> authorities = new HashMap<>();

        authorities.put(key,
                userDetails
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")));

        return authorities;
    }

}
