package com.swisscom.riskpilot.risk.controller;

import com.swisscom.riskpilot.risk.domain.AppUser;
import com.swisscom.riskpilot.risk.dto.JwtResponse;
import com.swisscom.riskpilot.risk.dto.LoginRequest;
import com.swisscom.riskpilot.risk.repository.AppUserRepository;
import com.swisscom.riskpilot.risk.security.JwtUtil;
import com.swisscom.riskpilot.risk.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final AppUserRepository appUserRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            AppUser appUser = appUserRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow();

            String token = jwtUtil.generateToken(userDetails, appUser.getRole());

            return ResponseEntity.ok(new JwtResponse(
                    token,
                    appUser.getUsername(),
                    appUser.getRole().name(),
                    jwtUtil.getExpirationMs()
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Invalid username or password\"}");
        }
    }
}
