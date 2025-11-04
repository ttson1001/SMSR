package com.example.smrsservice.security;


import com.example.smrsservice.entity.Account;
import com.example.smrsservice.repository.AccountRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final AccountRepository accountRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header)) {
            String token = header.startsWith("Bearer ") ? header.substring(7) : header;

            try {
                if (jwtTokenUtil.validateToken(token)) {
                    String email = jwtTokenUtil.extractEmail(token).trim().toLowerCase();

                    Account account = accountRepository.findWithRoleByEmail(email)
                            .orElse(null);

                    System.out.println("Account found: " + (account != null));
                    if (account != null) {
                        System.out.println("Account ID: " + account.getId());
                        System.out.println("Account email: [" + account.getEmail() + "]");
                    }
                    System.out.println("============================");

                    if (account != null && account.getRole() != null) {
                        List<SimpleGrantedAuthority> authorities =
                                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().getRoleName()));

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(account, null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else if (account != null) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(account, null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (JwtException ex) {
                System.out.println("JWT invalid: " + ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
