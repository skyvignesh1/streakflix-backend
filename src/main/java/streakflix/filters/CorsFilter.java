package streakflix.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CorsFilter extends OncePerRequestFilter {

    @Value("${org.streakflix.url}")
    private String streakFlixOrigin;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        response.addHeader("Access-Control-Allow-Origin", streakFlixOrigin);
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept");
        filterChain.doFilter(request, response);
    }
}
