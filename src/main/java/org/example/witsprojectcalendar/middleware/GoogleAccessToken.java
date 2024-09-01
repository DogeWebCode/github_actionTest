package org.example.witsprojectcalendar.middleware;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class GoogleAccessToken extends AbstractAuthenticationToken {
    private final String accessToken;

    public GoogleAccessToken(String accessToken) {
        super(null);
        this.accessToken = accessToken;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return accessToken;
    }

    @Override
    public Object getPrincipal() {
        return accessToken;
    }

}
