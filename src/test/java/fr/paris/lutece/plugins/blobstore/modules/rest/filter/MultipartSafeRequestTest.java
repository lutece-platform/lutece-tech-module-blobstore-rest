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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.paris.lutece.test.mocks.MockHttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Tests the request wrapper that reads the signed parameters of a multipart call from its query string only.
 */
public class MultipartSafeRequestTest
{
    /**
     * Builds a request with a content type, a query string and a body parameter.
     * 
     * @param strContentType
     *            the content type
     * @param strQueryString
     *            the query string
     * @return the request
     */
    private static MockHttpServletRequest request( String strContentType, String strQueryString )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( )
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public String getQueryString( )
            {
                return strQueryString;
            }
        };
        request.setContentType( strContentType );
        request.setParameter( "blobstore", "from.body" );

        return request;
    }

    /**
     * A multipart call exposes the decoded query string parameters and never the body ones.
     */
    @Test
    public void testMultipartReadsTheQueryStringOnly( )
    {
        MultipartSafeRequest request = new MultipartSafeRequest( request( "multipart/form-data; boundary=x", "blobstore=blobstore.db%2Fstore&blob_key=a&blob_key=b&flag" ) );

        assertEquals( "blobstore.db/store", request.getParameter( "blobstore" ) );
        assertArrayEquals( new String [ ] { "a", "b" }, request.getParameterValues( "blob_key" ) );
        assertEquals( "", request.getParameter( "flag" ) );
        assertNull( request.getParameter( "missing" ) );
        assertNull( request.getParameterValues( "missing" ) );
        assertEquals( List.of( "blobstore", "blob_key", "flag" ), Collections.list( request.getParameterNames( ) ) );
    }

    /**
     * The parameter map of a multipart call cannot be modified by a caller.
     */
    @Test
    public void testMultipartParameterMapIsReadOnly( )
    {
        Map<String, String [ ]> map = new MultipartSafeRequest( request( "MULTIPART/form-data", "blobstore=x" ) ).getParameterMap( );

        assertThrows( UnsupportedOperationException.class, ( ) -> map.put( "blobstore", new String [ ] { "y" } ) );
    }

    /**
     * A multipart call without query string has no parameter.
     */
    @Test
    public void testMultipartWithoutQueryString( )
    {
        MultipartSafeRequest request = new MultipartSafeRequest( request( "multipart/form-data", null ) );

        assertNull( request.getParameter( "blobstore" ) );
        assertEquals( 0, request.getParameterMap( ).size( ) );
    }

    /**
     * Any other call reads its parameters from the wrapped request.
     */
    @Test
    public void testOtherCallsDelegate( )
    {
        MultipartSafeRequest request = new MultipartSafeRequest( request( "application/x-www-form-urlencoded", "blobstore=from.query" ) );

        assertEquals( "from.body", request.getParameter( "blobstore" ) );
    }
}
