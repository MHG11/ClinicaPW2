package com.example.hospital.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/login/**´").permitAll()
                        .requestMatchers("/consultas/form", "/consultas/save").hasAnyRole("ADMIN", "SECRETARIA")
                        .requestMatchers("/pacientes/**").hasAnyRole("ADMIN", "SECRETARIA")
                        .requestMatchers("/consultas/list").hasAnyRole("ADMIN", "SECRETARIA", "PACIENTE")
                        .requestMatchers(HttpMethod.POST, "/usuario/cadastro").permitAll()
                        .requestMatchers("/usuario/dados").hasRole("ADMIN")
                        .requestMatchers("/usuario/perfis", "/usuario/*/perfil").hasRole("ADMIN")
                        .anyRequest().authenticated() // demais requisições exigem usuário autenticado
                )
                .formLogin(form -> form
                        .loginPage("/login") // URL da página de login que criaremos
                        .defaultSuccessUrl("/consultas/list", true)
                        .permitAll() // a página de login é acessível sem autenticação
                )
                .httpBasic(withDefaults()) // autenticação básica (usuário e senha no cabeçalho HTTP)
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .rememberMe(withDefaults()); // mantém o usuário autenticado após fechar o navegador
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}