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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.sun.jersey.core.header.FormDataContentDisposition;

import fr.paris.lutece.plugins.blobstore.modules.rest.util.constants.BlobStoreRestConstants;
import fr.paris.lutece.plugins.blobstore.service.BlobStoreFileItem;
import fr.paris.lutece.plugins.blobstore.service.BlobStorePlugin;
import fr.paris.lutece.plugins.blobstore.service.IBlobStoreService;
import fr.paris.lutece.plugins.blobstore.service.NoSuchBlobException;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.spring.SpringContextService;
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

        sbBase.append( RestConstants.BASE_PATH + BlobStorePlugin.PLUGIN_NAME );

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
            IBlobStoreService blobStoreService;

            try
            {
                blobStoreService = (IBlobStoreService) SpringContextService.getPluginBean( BlobStorePlugin.PLUGIN_NAME, strBlobStore );
                strDownloadUrl = blobStoreService.getFileUrl( strBlobKey );
            }
            catch( BeanDefinitionStoreException e )
            {
                AppLogService.error( BlobStoreRestConstants.MESSAGE_NO_SUCH_BLOBSTORE );
            }
            catch( NoSuchBeanDefinitionException e )
            {
                AppLogService.error( BlobStoreRestConstants.MESSAGE_NO_SUCH_BLOBSTORE );
            }
            catch( CannotLoadBeanClassException e )
            {
                AppLogService.error( BlobStoreRestConstants.MESSAGE_NO_SUCH_BLOBSTORE );
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

            IBlobStoreService blobStoreService;

            try
            {
                blobStoreService = (IBlobStoreService) SpringContextService.getPluginBean( BlobStorePlugin.PLUGIN_NAME, strBlobStore );

                BlobStoreFileItem fileItem = new BlobStoreFileItem( strBlobKey, blobStoreService );
                fileItem.delete( );
            }
            catch( BeanDefinitionStoreException e )
            {
                AppLogService.error( BlobStoreRestConstants.MESSAGE_NO_SUCH_BLOBSTORE );
            }
            catch( NoSuchBeanDefinitionException e )
            {
                AppLogService.error( BlobStoreRestConstants.MESSAGE_NO_SUCH_BLOBSTORE );
            }
            catch( CannotLoadBeanClassException e )
            {
                AppLogService.error( BlobStoreRestConstants.MESSAGE_NO_SUCH_BLOBSTORE );
            }
            catch( NoSuchBlobException e )
            {
                AppLogService.error( BlobStoreRestConstants.MESSAGE_NO_SUCH_BLOBSTORE );
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
     * @param strBlobStore
     *            the the blobstore service
     * @param blob
     *            the blob to create
     * @param blobDetail
     *            the blob detail
     * @return the id of the newly created blob
     */
    @POST
    @Path( BlobStoreRestConstants.PATH_CREATE_BLOBSTORE )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public String doCreateBlobStore( @FormParam( BlobStoreRestConstants.PARAMETER_BLOBSTORE ) String strBlobStore,
            @FormParam( BlobStoreRestConstants.PARAMETER_BLOB ) InputStream blob,
            @FormParam( BlobStoreRestConstants.PARAMETER_BLOB ) FormDataContentDisposition blobDetail )
    {
        String strBlobKey = StringUtils.EMPTY;

        if ( StringUtils.isNotBlank( strBlobStore ) && ( blob != null ) )
        {
            IBlobStoreService blobStoreService;

            try
            {
                blobStoreService = (IBlobStoreService) SpringContextService.getPluginBean( BlobStorePlugin.PLUGIN_NAME, strBlobStore );
                strBlobKey = blobStoreService.storeInputStream( blob );

                String strJSON = BlobStoreFileItem.buildFileMetadata( blobDetail.getFileName( ), blobDetail.getSize( ), strBlobKey, blobDetail.getType( ) );

                if ( AppLogService.isDebugEnabled( ) )
                {
                    AppLogService.debug( "Storing " + blobDetail.getName( ) + " with : " + strJSON );
                }

                strBlobKey = blobStoreService.store( strJSON.getBytes( ) );
            }
            catch( BeanDefinitionStoreException e )
            {
                AppLogService.error( BlobStoreRestConstants.MESSAGE_NO_SUCH_BLOBSTORE );
            }
            catch( NoSuchBeanDefinitionException e )
            {
                AppLogService.error( BlobStoreRestConstants.MESSAGE_NO_SUCH_BLOBSTORE );
            }
            catch( CannotLoadBeanClassException e )
            {
                AppLogService.error( BlobStoreRestConstants.MESSAGE_NO_SUCH_BLOBSTORE );
            }
            finally
            {
                IOUtils.closeQuietly( blob );
            }
        }
        else
        {
            AppLogService.error( BlobStoreRestConstants.MESSAGE_MANDATORY_FIELDS );
        }

        return strBlobKey;
    }
}
