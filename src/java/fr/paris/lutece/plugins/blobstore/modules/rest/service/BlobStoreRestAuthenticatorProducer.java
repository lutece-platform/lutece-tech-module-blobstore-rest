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
package fr.paris.lutece.plugins.blobstore.modules.rest.service;

import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.util.signrequest.NoSecurityAuthenticator;
import fr.paris.lutece.util.signrequest.RequestAuthenticator;
import fr.paris.lutece.util.signrequest.cdi.AbstractSignRequestAuthenticatorProducer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Produces the authenticator checking the signature of the blobstore REST calls.
 */
@ApplicationScoped
public class BlobStoreRestAuthenticatorProducer extends AbstractSignRequestAuthenticatorProducer
{
    /** The configuration prefix the authenticator is built from. */
    private static final String CONFIG_PREFIX = "blobstore-rest.requestAuthenticator";

    /** The configuration key naming the authenticator to build. */
    private static final String PROPERTY_AUTHENTICATOR_NAME = CONFIG_PREFIX + ".name";

    /**
     * Builds the authenticator from the blobstore-rest.requestAuthenticator configuration keys.
     * 
     * @return the authenticator of the blobstore REST calls
     * @throws AppException
     *             when the configuration names no known authenticator, which would leave the calls unchecked
     */
    @Produces
    @ApplicationScoped
    @BlobStoreRestRequestAuthenticator
    public RequestAuthenticator produceBlobStoreRestRequestAuthenticator( )
    {
        RequestAuthenticator authenticator = produceRequestAuthenticator( CONFIG_PREFIX );

        if ( authenticator instanceof NoSecurityAuthenticator )
        {
            throw new AppException(
                    PROPERTY_AUTHENTICATOR_NAME + " names no known authenticator, so every blobstore REST call would be served without a signature check" );
        }

        return authenticator;
    }
}
