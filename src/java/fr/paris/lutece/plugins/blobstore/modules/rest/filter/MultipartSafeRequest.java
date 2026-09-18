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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;

/**
 * Serves the parameters of a multipart request from the query string alone.
 * 
 * Reading a parameter of a multipart body makes the container parse that body and consume the entity, which leaves nothing for the resource method to read.
 * Any other request is served by the container as usual.
 */
public class MultipartSafeRequest extends HttpServletRequestWrapper
{
    /** The content type prefix of the bodies this wrapper keeps unread. */
    private static final String MULTIPART_PREFIX = "multipart/";

    /** The parameters of the query string, or <code>null</code> when the container may serve the parameters itself. */
    private final Map<String, String [ ]> _mapQueryParameters;

    /**
     * Wraps a request.
     * 
     * @param request
     *            the request to wrap
     */
    public MultipartSafeRequest( HttpServletRequest request )
    {
        super( request );
        _mapQueryParameters = isMultipart( request ) ? parseQueryString( request.getQueryString( ) ) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParameter( String strName )
    {
        if ( _mapQueryParameters == null )
        {
            return super.getParameter( strName );
        }

        String [ ] strValues = _mapQueryParameters.get( strName );

        return ( strValues == null ) ? null : strValues [0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String [ ] getParameterValues( String strName )
    {
        if ( _mapQueryParameters == null )
        {
            return super.getParameterValues( strName );
        }

        String [ ] strValues = _mapQueryParameters.get( strName );

        return ( strValues == null ) ? null : strValues.clone( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String [ ]> getParameterMap( )
    {
        if ( _mapQueryParameters == null )
        {
            return super.getParameterMap( );
        }

        return Collections.unmodifiableMap( _mapQueryParameters );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getParameterNames( )
    {
        if ( _mapQueryParameters == null )
        {
            return super.getParameterNames( );
        }

        return Collections.enumeration( _mapQueryParameters.keySet( ) );
    }

    /**
     * Tells whether a request carries a multipart body.
     * 
     * @param request
     *            the request
     * @return <code>true</code> when the body is multipart
     */
    private static boolean isMultipart( HttpServletRequest request )
    {
        String strContentType = request.getContentType( );

        return ( strContentType != null ) && strContentType.toLowerCase( Locale.ROOT ).startsWith( MULTIPART_PREFIX );
    }

    /**
     * Reads the parameters of a query string.
     * 
     * @param strQueryString
     *            the query string, which may be <code>null</code>
     * @return the parameters, in the order the query string carries them
     */
    private static Map<String, String [ ]> parseQueryString( String strQueryString )
    {
        Map<String, List<String>> mapValues = new LinkedHashMap<>( );

        if ( StringUtils.isNotEmpty( strQueryString ) )
        {
            for ( String strPair : strQueryString.split( "&" ) )
            {
                int nIndex = strPair.indexOf( '=' );
                String strName = ( nIndex < 0 ) ? strPair : strPair.substring( 0, nIndex );
                String strValue = ( nIndex < 0 ) ? StringUtils.EMPTY : strPair.substring( nIndex + 1 );

                if ( StringUtils.isNotEmpty( strName ) )
                {
                    mapValues.computeIfAbsent( decode( strName ), strKey -> new ArrayList<>( ) ).add( decode( strValue ) );
                }
            }
        }

        Map<String, String [ ]> mapParameters = new LinkedHashMap<>( );
        mapValues.forEach( ( strKey, listValues ) -> mapParameters.put( strKey, listValues.toArray( new String [ 0 ] ) ) );

        return mapParameters;
    }

    /**
     * Decodes a query string token.
     * 
     * @param strToken
     *            the token
     * @return the decoded token
     */
    private static String decode( String strToken )
    {
        return URLDecoder.decode( strToken, StandardCharsets.UTF_8 );
    }
}
