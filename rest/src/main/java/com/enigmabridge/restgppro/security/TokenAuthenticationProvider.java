/*
 * Copyright (c) 2016 Enigma Bridge Ltd.
 *
 * This file is part of the GlobalPlatformProREST project.
 *
 *     GlobalPlatformProREST is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GlobalPlatformProREST is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GlobalPlatformProREST.  If not, see <http://www.gnu.org/licenses/>.
 *
 *     If you have any support question, use the GitHub facilities. Visit http://enigmabridge.com
 *     if you want to speak to us directly.
 */

package com.enigmabridge.restgppro.security;

/**
 * Created by Enigma Bridge Ltd (dan) on 06/10/2016.
 */
import com.enigmabridge.restgppro.ApiConfig;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;


public class TokenAuthenticationProvider implements AuthenticationProvider {
    private final static Logger LOG = LoggerFactory.getLogger(TokenAuthenticationProvider.class);

    private TokenService tokenService;

    public TokenAuthenticationProvider(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final Object principal = authentication.getPrincipal();
        LOG.debug("Principal: {}" + principal);

        Optional<String> token = (Optional) principal;
        if (!token.isPresent() || token.get().isEmpty()) {
            throw new BadCredentialsException("Invalid token");
        }

        if (tokenService.containsManagementToken(token.get())){
            return new UsernamePasswordAuthenticationToken(token.get(), null,
                    AuthorityUtils.commaSeparatedStringToAuthorityList(ApiConfig.MANAGEMENT_ROLE));
        }

        if (tokenService.containsBusinessToken(token.get())){
            return new UsernamePasswordAuthenticationToken(token.get(), null,
                    AuthorityUtils.commaSeparatedStringToAuthorityList(ApiConfig.BUSINESS_ROLE));
        }

        throw new BadCredentialsException("Invalid token or token expired");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}
