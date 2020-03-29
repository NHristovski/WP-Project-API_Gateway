package hristovski.nikola.api_gateway.filters;

import com.netflix.discovery.converters.Auto;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import hristovski.nikola.api_gateway.configuration.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class UsernameFilter extends ZuulFilter {

    @Autowired
    private JwtConfig jwtConfig;


    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();


        String header = request.getHeader(jwtConfig.getHeader());

        log.info("Header: {}", header);

        if (header == null || !header.startsWith(jwtConfig.getPrefix())) {
            return null;
        }

        String token = header.replace(jwtConfig.getPrefix(), "");

        log.info("Token: {}", token);


        // 4. Validate the token
        Claims claims = Jwts.parser()
                .setSigningKey(jwtConfig.getSecret().getBytes())
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();

        ctx.addZuulRequestHeader("username", username);

        log.info("Added the username {} as header", username);


        return null;
    }
}

