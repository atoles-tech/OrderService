package atl.web.order_service.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import atl.web.order_service.client.AuthServiceClient;
import atl.web.order_service.dto.ValidateTokenRequestDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{

    private AuthServiceClient authServiceClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String jwt = parseJwt(request);

        ValidateTokenRequestDto jwtRequest = new ValidateTokenRequestDto(jwt);

        if (jwt != null && authServiceClient.validateToken(jwtRequest)) {
            String username = authServiceClient.exrtactEmail(jwtRequest);

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authServiceClient.extractRole(jwtRequest))); 
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(username, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      
        filterChain.doFilter(request, response);
        
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        
        if (headerAuth != null && headerAuth.startsWith("Bearer ") && headerAuth.length() > 7) {
            return headerAuth.substring(7);
        }
        
        return null;
    }
    
}
