package org.example.witsprojectcalendar.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import java.io.IOException;

@Log4j2
public class GoogleAccessTokenFilter extends OncePerRequestFilter {

    private static final String GOOGLE_TOKENINFO_ENDPOINT = "https://oauth2.googleapis.com/tokeninfo?access_token={access_token}";


    @Override
    protected void doFilterInternal(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String accessToken = extractAccessToken(request);

        if (accessToken != null) {
            try {
                GoogleTokenInfo tokenInfo = validateGoogleAccessToken(accessToken);
                if (tokenInfo != null && tokenInfo.isValid()) {
                    SecurityContextHolder.getContext().setAuthentication(new GoogleAccessToken(accessToken));
                }
            } catch (Exception ex) {
                log.error("Error Validating Google Access Token", ex);
            }
        }

        filterChain.doFilter(request, response);
    }


    private String extractAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private GoogleTokenInfo validateGoogleAccessToken(String accessToken) {
        String url = GOOGLE_TOKENINFO_ENDPOINT.replace("{access_token}", accessToken);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<GoogleTokenInfo> response = restTemplate.getForEntity(url, GoogleTokenInfo.class);

        return response.getBody();
    }
}

@Data
@AllArgsConstructor
class GoogleTokenInfo {
    private String azp;
    private String aud;
    private String sub;
    private String scope;
    private String exp;
    private String email;
    private String email_verified;

    public boolean isValid() {
        return email != null && Boolean.parseBoolean(email_verified);
    }
}