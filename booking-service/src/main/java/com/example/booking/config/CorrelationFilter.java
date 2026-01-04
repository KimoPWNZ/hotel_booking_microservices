package com.example.booking.config;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CorrelationFilter extends OncePerRequestFilter {
    public static final String CID = "X-Correlation-Id";
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String cid = request.getHeader(CID);
        if (cid == null || cid.isBlank()) cid = UUID.randomUUID().toString();
        MDC.put(CID, cid);
        response.addHeader(CID, cid);
        try { filterChain.doFilter(request, response); }
        finally { MDC.clear(); }
    }
}