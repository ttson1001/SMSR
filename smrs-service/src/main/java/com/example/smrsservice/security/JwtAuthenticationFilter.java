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

        // Lấy token từ header
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header)) {
            String token = header.startsWith("Bearer ") ? header.substring(7) : header;

            try {
                if (jwtTokenUtil.validateToken(token)) {
                    String email = jwtTokenUtil.extractEmail(token);

                    // Fetch Account với role luôn để tránh LazyInitializationException
                    Account account = accountRepository.findWithRoleByEmail(email)
                            .orElse(null);

                    if (account != null && account.getRole() != null) {
                        // Gắn quyền từ Role
                        List<SimpleGrantedAuthority> authorities =
                                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().getRoleName()));

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(account, null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else if (account != null) {
                        // Nếu không có role, vẫn cho qua với quyền trống
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(account, null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (JwtException ex) {
                // Token không hợp lệ -> không set authentication
                System.out.println("JWT invalid: " + ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
