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
package fr.paris.lutece.plugins.blobstore.modules.rest.rs;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;

import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.blobstore.modules.rest.filter.BlobStoreRestAuthentication;
import fr.paris.lutece.plugins.blobstore.modules.rest.util.constants.BlobStoreRestConstants;
import fr.paris.lutece.plugins.blobstore.service.BlobStoreFileItem;
import fr.paris.lutece.plugins.blobstore.service.BlobStorePlugin;
import fr.paris.lutece.plugins.blobstore.service.IBlobStoreService;
import fr.paris.lutece.plugins.blobstore.service.NoSuchBlobException;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.util.html.HtmlTemplate;

/**
 * 
 * BlobStoreRest
 * 
 */
@Path( RestConstants.BASE_PATH + BlobStorePlugin.PLUGIN_NAME )
@BlobStoreRestAuthentication
public class BlobStoreRest
{
    /**
     * Get the wadl.xml content
     * 
     * @param request
     *            {@link HttpServletRequest}
     * @return the content of wadl.xml
     */
    @GET
    @Path( BlobStoreRestConstants.PATH_WADL )
    @Produces( MediaType.APPLICATION_XML )
    public String getWADL( @Context HttpServletRequest request )
    {
        StringBuilder sbBase = new StringBuilder( AppPathService.getBaseUrl( request ) );

        if ( sbBase.toString( ).endsWith( BlobStoreRestConstants.SLASH ) )
        {
            sbBase.deleteCharAt( sbBase.length( ) - 1 );
        }

        sbBase.append( RestConstants.APP_PATH + BlobStorePlugin.PLUGIN_NAME );

        Map<String, Object> model = new HashMap<String, Object>( );
        model.put( BlobStoreRestConstants.MARK_BASE_URL, sbBase.toString( ) );

        HtmlTemplate t = AppTemplateService.getTemplate( BlobStoreRestConstants.TEMPLATE_WADL, request.getLocale( ), model );

        return t.getHtml( );
    }

    /**
     * Get the file url
     * 
     * @param strBlobStore
     *            the blobstore
     * @param strBlobKey
     *            the blob key
     * @return the file url
     */
    @GET
    @Path( BlobStoreRestConstants.PATH_FILE_URL )
    @Produces( MediaType.TEXT_PLAIN )
    public String getFileUrl( @PathParam( BlobStoreRestConstants.PARAMETER_BLOBSTORE ) String strBlobStore,
            @PathParam( BlobStoreRestConstants.PARAMETER_BLOB_KEY ) String strBlobKey )
    {
        String strDownloadUrl = StringUtils.EMPTY;

        if ( StringUtils.isNotBlank( strBlobStore ) && StringUtils.isNotBlank( strBlobKey ) )
        {
            IBlobStoreService blobStoreService = getBlobStoreService( strBlobStore );

            if ( blobStoreService != null )
            {
                strDownloadUrl = blobStoreService.getFileUrl( strBlobKey );
            }
        }
        else
        {
            AppLogService.error( BlobStoreRestConstants.MESSAGE_MANDATORY_FIELDS );
        }

        return strDownloadUrl;
    }

    /**
     * Delete a blob
     * 
     * @param strBlobKey
     *            the blob key
     * @param strBlobStore
     *            the blobstore
     * @return the blob key
     */
    @POST
    @Path( BlobStoreRestConstants.PATH_DELETE_BLOBSTORE )
    @Produces( MediaType.TEXT_HTML )
    @Consumes( MediaType.APPLICATION_FORM_URLENCODED )
    public String doDeleteBlobStore( @FormParam( BlobStoreRestConstants.PARAMETER_BLOB_KEY ) String strBlobKey,
            @FormParam( BlobStoreRestConstants.PARAMETER_BLOBSTORE ) String strBlobStore )
    {
        String strResponse = StringUtils.EMPTY;

        if ( StringUtils.isNotBlank( strBlobKey ) && StringUtils.isNotBlank( strBlobStore ) )
        {
            strResponse = strBlobKey;

            IBlobStoreService blobStoreService = getBlobStoreService( strBlobStore );

            if ( blobStoreService != null )
            {
                try
                {
                    BlobStoreFileItem fileItem = new BlobStoreFileItem( strBlobKey, blobStoreService );
                    fileItem.delete( );
                }
                catch( NoSuchBlobException e )
                {
                    AppLogService.error( BlobStoreRestConstants.MESSAGE_NO_SUCH_BLOBSTORE, e );
                }
            }
        }
        else
        {
            AppLogService.error( BlobStoreRestConstants.MESSAGE_MANDATORY_FIELDS );
        }

        return strResponse;
    }

    /**
     * Create a blob
     * 
     * @param strBlobStoreFromUrl
     *            the blobstore service name, read from the query string
     * @param listParts
     *            the parts of the multipart body, holding the blob and the blobstore service name
     * @return the id of the newly created blob
     */
    @POST
    @Path( BlobStoreRestConstants.PATH_CREATE_BLOBSTORE )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public String doCreateBlobStore( @QueryParam( BlobStoreRestConstants.PARAMETER_BLOBSTORE ) String strBlobStoreFromUrl,
            List<EntityPart> listParts )
    {
        String strBlobKey = StringUtils.EMPTY;
        EntityPart blob = getPart( listParts, BlobStoreRestConstants.PARAMETER_BLOB );
        String strBlobStore = StringUtils.isNotBlank( strBlobStoreFromUrl ) ? strBlobStoreFromUrl
                : getPartText( listParts, BlobStoreRestConstants.PARAMETER_BLOBSTORE );

        if ( StringUtils.isNotBlank( strBlobStore ) && ( blob != null ) )
        {
            IBlobStoreService blobStoreService = getBlobStoreService( strBlobStore );

            if ( blobStoreService != null )
            {
                try ( BoundedInputStream content = BoundedInputStream.builder( ).setInputStream( blob.getContent( ) ).get( ) )
                {
                    strBlobKey = blobStoreService.storeInputStream( content );

                    String strJSON = BlobStoreFileItem.buildFileMetadata( blob.getFileName( ).orElse( blob.getName( ) ), content.getCount( ), strBlobKey,
                            blob.getMediaType( ).toString( ) );

                    AppLogService.debug( "Storing {} with : {}", blob.getName( ), strJSON );

                    strBlobKey = blobStoreService.store( strJSON.getBytes( ) );
                }
                catch( IOException e )
                {
                    AppLogService.error( e.getMessage( ), e );
                }
            }
        }
        else
        {
            AppLogService.error( BlobStoreRestConstants.MESSAGE_MANDATORY_FIELDS );
        }

        return strBlobKey;
    }

    /**
     * Gets the blob store service carrying a name.
     * 
     * @param strBlobStore
     *            the blob store name, read from the request
     * @return the blob store service, or <code>null</code> when no single service carries that name
     */
    private IBlobStoreService getBlobStoreService( String strBlobStore )
    {
        if ( StringUtils.isBlank( strBlobStore ) )
        {
            AppLogService.error( BlobStoreRestConstants.MESSAGE_NO_SUCH_BLOBSTORE );

            return null;
        }

        try
        {
            return CDI.current( ).select( IBlobStoreService.class, NamedLiteral.of( strBlobStore ) ).get( );
        }
        catch( AmbiguousResolutionException | UnsatisfiedResolutionException e )
        {
            AppLogService.error( BlobStoreRestConstants.MESSAGE_NO_SUCH_BLOBSTORE, e );
        }

        return null;
    }

    /**
     * Gets a part of a multipart body by its name.
     * 
     * @param listParts
     *            the parts of the body
     * @param strName
     *            the part name
     * @return the part, or <code>null</code> when the body carries no part of that name
     */
    private EntityPart getPart( List<EntityPart> listParts, String strName )
    {
        if ( listParts == null )
        {
            return null;
        }

        return listParts.stream( ).filter( part -> strName.equals( part.getName( ) ) ).findFirst( ).orElse( null );
    }

    /**
     * Reads a part of a multipart body as text.
     * 
     * @param listParts
     *            the parts of the body
     * @param strName
     *            the part name
     * @return the part content, or an empty string when the body carries no part of that name
     */
    private String getPartText( List<EntityPart> listParts, String strName )
    {
        EntityPart part = getPart( listParts, strName );

        if ( part == null )
        {
            return StringUtils.EMPTY;
        }

        try
        {
            return part.getContent( String.class );
        }
        catch( IOException e )
        {
            AppLogService.error( e.getMessage( ), e );

            return StringUtils.EMPTY;
        }
    }
}
