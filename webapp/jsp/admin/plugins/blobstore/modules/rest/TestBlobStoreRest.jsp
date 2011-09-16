<%@page import="fr.paris.lutece.portal.service.util.AppPathService"%>

<html>
    <head>
        <title>BlobStore - REST webservices test page</title>
        <base href="<%= AppPathService.getBaseUrl( request ) %>" />
        <link rel="stylesheet" type="text/css" href="css/portal_admin.css" title="lutece_admin" />
        <script type="text/javascript">
            function onFileUrlView(  ) {
                var blobKey = document.formGetFileUrl.blob_key.value;
                var blobStore = document.formGetFileUrl.blobstore.value;
                document.location= 'rest/blobstore/' + blobStore + '/' + blobKey;
            }
        </script>
    </head>
    <body>
        <div id="content" >
            <h1>BlobStore - REST webservices test page </h1>
            <div class="content-box">
	            <div class="highlight-box">
	                <h2>View WADL</h2>
	                <form action="rest/blobstore/wadl">
	                    <br/>
	                    <input class="button" type="submit" value="View WADL" />
	                </form>
	            </div>
	            
	            <div class="highlight-box">
	                <h2>View blob URL</h2>
	                <form name="formGetFileUrl">
	                    <label for="blob_key">Blob Key * : </label>
	                    <input type="text" name="blob_key" />
	                    <br />
	                    <label for="blobstore">BlobStore * : </label>
	                    <input type="text" name="blobstore" />
	                    <br />
	                    <input class="button" type="button" value="View" onclick="javascript:onFileUrlView(  )"/>
	                </form>
	            </div>
	            
	            <div class="highlight-box">
	                <h2>Create a blob</h2>
	                <form action="rest/blobstore/create" method="post" enctype="multipart/form-data">
	                    <label for="blobstore">BlobStore * : </label>
	                    <input type="text" name="blobstore" />
	                    <br />
	                    <label for="blob">File * :</label>
	                    <input type="file" name="blob" />
	                    <br />
	                    <input class="button" type="submit" value="Create" />
	                </form>
	            </div>
	            
	            <div class="highlight-box">
	                <h2>Delete a blob</h2>
	                <form action="rest/blobstore/delete" method="post">
	                    <label for="blob_key">Blob Key * : </label>
	                    <input type="text" name="blob_key" />
	                    <br />
	                    <label for="blobstore">BlobStore * : </label>
	                    <input type="text" name="blobstore" />
	                    <br />
	                    <input class="button" type="submit" value="Delete" />
	                </form>
	            </div>
			</div>
		</div>
    </body>
</html>
