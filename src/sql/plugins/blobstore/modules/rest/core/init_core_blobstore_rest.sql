-- liquibase formatted sql
-- changeset blobstore-rest:init_core_blobstore_rest.sql
-- preconditions onFail:MARK_RAN onError:WARN
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url,id_order) VALUES ('BLOBSTORE_REST_TEST','module.blobstore.rest.adminFeature.testBlobStoreRest.name',0,'jsp/admin/plugins/blobstore/modules/rest/TestBlobStoreRest.jsp','module.blobstore.rest.adminFeature.testBlobStoreRest.description',0,'blobstore-rest','SYSTEM','ti ti-api',NULL,NULL);
INSERT INTO core_user_right (id_right,id_user) VALUES ('BLOBSTORE_REST_TEST',1);
