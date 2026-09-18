/*
 * Copyright (c) 2002-2020, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.blobstore.modules.rest.filter;

import fr.paris.lutece.plugins.blobstore.modules.rest.service.BlobStoreRestRequestAuthenticator;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.signrequest.RequestAuthenticator;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Refuses a blobstore REST call whose signature does not authenticate it.
 */
@Provider
@BlobStoreRestAuthentication
@Priority( Priorities.AUTHENTICATION )
public class BlobStoreRestAuthenticatorRequestFilter implements ContainerRequestFilter
{
    /** The configuration key switching the signature check off. */
    private static final String PROPERTY_SECURITY_ACTIVATED = "blobstore-rest.security.activated";

    @Inject
    private HttpServletRequest _httpRequest;

    @Inject
    @BlobStoreRestRequestAuthenticator
    private Instance<RequestAuthenticator> _requestAuthenticators;

    @Inject
    @ConfigProperty( name = PROPERTY_SECURITY_ACTIVATED, defaultValue = "true" )
    private boolean _bSecurityActivated;

    /** The authenticator, resolved once so that a misconfigured name fails here rather than on a call. */
    private RequestAuthenticator _requestAuthenticator;

    /**
     * Resolves the authenticator, whether the signature check is switched on or off.
     */
    @PostConstruct
    public void init( )
    {
        _requestAuthenticator = _requestAuthenticators.get( );

        if ( !_bSecurityActivated )
        {
            AppLogService.info( "BlobStoreRest - signature check switched off by {}, every call is served unchecked", PROPERTY_SECURITY_ACTIVATED );
        }
    }

    /**
     * Aborts the call with a 401 when its signature does not authenticate it, unless the signature check is switched off.
     * 
     * @param requestContext
     *            the request context
     */
    @Override
    public void filter( ContainerRequestContext requestContext )
    {
        if ( !_bSecurityActivated )
        {
            return;
        }

        if ( !_requestAuthenticator.isRequestAuthenticated( new MultipartSafeRequest( _httpRequest ) ) )
        {
            AppLogService.debug( "BlobStoreRest - unauthenticated call refused : {} {}", _httpRequest.getMethod( ), _httpRequest.getPathInfo( ) );
            requestContext.abortWith( Response.status( Response.Status.UNAUTHORIZED ).build( ) );
        }
    }
}
