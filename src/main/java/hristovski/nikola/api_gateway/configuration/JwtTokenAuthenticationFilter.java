package hristovski.nikola.api_gateway.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtConfig jwtConfig;

    public JwtTokenAuthenticationFilter(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. get the authentication header. Tokens are supposed to be passed in the authentication header
        String header = request.getHeader(jwtConfig.getHeader());

        log.info("Header: {}", header);
        // 2. validate the header and check the prefix
        if (header == null || !header.startsWith(jwtConfig.getPrefix())) {
            log.info("Not valid header. Continuing..");
            chain.doFilter(request, response);        // If not valid, go to the next filter.
            return;
        }

        // If there is no token provided and hence the user won't be authenticated.
        // It's Ok. Maybe the user accessing a public path or asking for a token.

        // All secured paths that needs a token are already defined and secured in config class.
        // And If user tried to access without access token, then he won't be authenticated and an exception will be thrown.

        // 3. Get the token
        String token = header.replace(jwtConfig.getPrefix(), "");

        log.info("Token: {}", token);
        try {    // exceptions might be thrown in creating the claims if for example the token is expired

            // 4. Validate the token
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtConfig.getSecret().getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            if (username != null) {

                @SuppressWarnings("unchecked")
                List<String> authorities = (List<String>) claims.get("authorities");

                // 5. Create auth object
                // UsernamePasswordAuthenticationToken: A built-in object, used by spring to represent the current authenticated / being authenticated user.
                // It needs a list of authorities, which has type of GrantedAuthority interface, where SimpleGrantedAuthority is an implementation of that interface
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null, authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

                // 6. Authenticate the user
                // Now, user is authenticated
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("Successful auth");

            } else {
                log.info("Username was null!");
            }

        } catch (Exception e) {
            // In case of failure. Make sure it's clear; so guarantee user won't be authenticated
            log.error("Failed to authenticate!", e);
            SecurityContextHolder.clearContext();
        }


        chain.doFilter(request, response);
    }

}
