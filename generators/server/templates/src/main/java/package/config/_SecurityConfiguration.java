package <%=packageName%>.config;
<% if (authenticationType == 'session' || authenticationType == 'jwt') { %>
import <%=packageName%>.security.*;<% } %><% if (authenticationType == 'session') { %>
import <%=packageName%>.web.filter.CsrfCookieGeneratorFilter;<% } %><% if (authenticationType == 'jwt') { %>
import <%=packageName%>.security.jwt.*;<% } %>
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;<% if (authenticationType == 'session') { %>
import org.springframework.core.env.Environment;<% } %>
import org.springframework.http.HttpMethod;<% if (authenticationType == 'oauth2' || authenticationType == 'jwt') { %>
import org.springframework.security.authentication.AuthenticationManager;<% } %>
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;<% if (authenticationType == 'jwt' || authenticationType == 'oauth2') { %>
import org.springframework.security.config.http.SessionCreationPolicy;<% } %><% if (clusteredHttpSession == 'hazelcast') { %>
import org.springframework.security.core.session.SessionRegistry;<% } %>
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
<% if (authenticationType == 'session') { %>
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.csrf.CsrfFilter;<% } %>

import javax.inject.Inject;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {<% if (authenticationType == 'session') { %>

    @Inject
    private Environment env;

    @Inject
    private AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler;

    @Inject
    private AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler;

    @Inject
    private AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler;<% } if (authenticationType == 'session' || authenticationType == 'jwt') { %>

    @Inject
    private Http401UnauthorizedEntryPoint authenticationEntryPoint;<% } %>

    @Inject
    private UserDetailsService userDetailsService;<% if (authenticationType == 'session') { %>

    @Inject
    private RememberMeServices rememberMeServices;<% } %><% if (clusteredHttpSession == 'hazelcast') { %>

    @Inject
    private SessionRegistry sessionRegistry;<% } %><% if (authenticationType == 'jwt') { %>

    @Inject
    private TokenProvider tokenProvider;<% } %>

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Inject
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
            .antMatchers(HttpMethod.OPTIONS, "/**")
            .antMatchers("/app/**/*.{js,html}")
            .antMatchers("/bower_components/**")
            .antMatchers("/i18n/**")
            .antMatchers("/content/**")
            .antMatchers("/swagger-ui/index.html")<% if (authenticationType == 'oauth2') { %>
            .antMatchers("/api/register")
            .antMatchers("/api/activate")
            .antMatchers("/api/account/reset_password/init")
            .antMatchers("/api/account/reset_password/finish")<% } %>
            .antMatchers("/test/**")<% if (devDatabaseType != 'h2Disk' && devDatabaseType != 'h2Memory') { %>;<% } else { %>
            .antMatchers("/h2-console/**");<% } %>
    }<% if (authenticationType == 'session' || authenticationType == 'jwt') { %>

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http<% if (clusteredHttpSession == 'hazelcast') { %>
            .sessionManagement()
            .maximumSessions(32) // maximum number of concurrent sessions for one user
            .sessionRegistry(sessionRegistry)
            .and().and()<% } %><% if (authenticationType == 'session') { %>
            .csrf()<% if (websocket == 'spring-websocket') { %>
            .ignoringAntMatchers("/websocket/**")<% } %>
        .and()
            .addFilterAfter(new CsrfCookieGeneratorFilter(), CsrfFilter.class)<% } %>
            .exceptionHandling()<% if (authenticationType == 'session') { %>
            .accessDeniedHandler(new CustomAccessDeniedHandler())<% } %>
            .authenticationEntryPoint(authenticationEntryPoint)<% if (authenticationType == 'session') { %>
        .and()
            .rememberMe()
            .rememberMeServices(rememberMeServices)
            .rememberMeParameter("remember-me")
            .key(env.getProperty("jhipster.security.rememberme.key"))
        .and()
            .formLogin()
            .loginProcessingUrl("/api/authentication")
            .successHandler(ajaxAuthenticationSuccessHandler)
            .failureHandler(ajaxAuthenticationFailureHandler)
            .usernameParameter("j_username")
            .passwordParameter("j_password")
            .permitAll()
        .and()
            .logout()
            .logoutUrl("/api/logout")
            .logoutSuccessHandler(ajaxLogoutSuccessHandler)
            .deleteCookies("JSESSIONID", "CSRF-TOKEN"<% if (clusteredHttpSession == 'hazelcast') { %>, "hazelcast.sessionId"<% } %>)
            .permitAll()<% } %>
        .and()<% if (authenticationType == 'jwt') { %>
            .csrf()
            .disable()<% } %>
            .headers()
            .frameOptions()
            .disable()
        .and()<% if (authenticationType == 'jwt') { %>
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()<% } %>
            .authorizeRequests()
            .antMatchers("/api/register").permitAll()
            .antMatchers("/api/activate").permitAll()
            .antMatchers("/api/authenticate").permitAll()
            .antMatchers("/api/account/reset_password/init").permitAll()
            .antMatchers("/api/account/reset_password/finish").permitAll()
            .antMatchers("/api/logs/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/api/audits/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/api/**").authenticated()<% if (websocket == 'spring-websocket') { %>
            .antMatchers("/websocket/tracker").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/websocket/**").permitAll()<% } %>
            .antMatchers("/metrics/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/health/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/trace/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/dump/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/shutdown/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/beans/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/configprops/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/info/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/autoconfig/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/env/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/mappings/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/liquibase/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/v2/api-docs/**").permitAll()
            .antMatchers("/configuration/security").permitAll()
            .antMatchers("/configuration/ui").permitAll()
            .antMatchers("/swagger-ui/index.html").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/protected/**").authenticated() <%if (authenticationType != 'jwt') { %>;<% } %><% if (authenticationType == 'jwt') { %>
        .and()
            .apply(securityConfigurerAdapter());<% } %>

    }<% } %><% if (authenticationType == 'oauth2') { %>

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic().realmName("<%= baseName %>")
            .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .requestMatchers().antMatchers("/oauth/authorize")
            .and()
                .authorizeRequests()
                .antMatchers("/oauth/authorize").authenticated();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }<% } %><% if (authenticationType == 'jwt') { %>

    private JWTConfigurer securityConfigurerAdapter() {
        return new JWTConfigurer(tokenProvider);
    }<% } %>

    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }
}
