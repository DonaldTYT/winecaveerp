package com.kikyosoft.rpcservlet;

import java.util.Vector;

import org.json.JSONObject;

import com.kikyosoft.config.LegacyToSpringBridge;
import com.kikyosoft.graphql.SchemaReader;
import com.kikyosoft.rpccall.RpcConnection;
import com.kikyosoft.rpccall.RpcServlet;
import com.kikyosoft.service.SaleorMediaService;
import com.kikyosoft.utils.VectorUtil;
import com.kikyosoft.wrapper.CategorySyncWrapper;
import com.kikyosoft.wrapper.ProductMediaSyncWrapper;
import com.kikyosoft.wrapper.ProductSyncWrapper;
import com.kikyosoft.wrapper.ProductTypeSyncWrapper;
import com.kikyosoft.wrapper.ProductVariantSyncWrapper;
import com.kyoko.common.CoreLog;
import com.kyoko.common.StringReturnCallback;

public class SaleorSyncRpc implements RpcServlet {
	RpcConnection conn;
	@Override
	public void init_servlet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close_servlet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConnection(RpcConnection p_conn) {
			conn = p_conn;
	}

	@Override
	public String ping() {
		// TODO Auto-generated method stub
		return "OK  SaleorSyncRpc Version 1.0";
	}

	public String getProductCategory() {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		CategorySyncWrapper catSync = LegacyToSpringBridge.instance(CategorySyncWrapper.class);
		try {
			String rtn = catSync.exportCategories();
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}

	public String deleteProductCategory(Vector args) {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		CategorySyncWrapper catSync = LegacyToSpringBridge.instance(CategorySyncWrapper.class);
		try {
			String rtn = catSync.deleteAllCategories(args);
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}

	public String insertProductCategory(String jsonStr) {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		CategorySyncWrapper catSync = LegacyToSpringBridge.instance(CategorySyncWrapper.class);
		try {
			String rtn = catSync.importCategories(jsonStr);
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}

	public String getProductType() {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		ProductTypeSyncWrapper ptSync = LegacyToSpringBridge.instance(ProductTypeSyncWrapper.class);
		try {
			String rtn = ptSync.exportProductTypes();
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}
	
	public String deleteProductType(Vector args) {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		ProductTypeSyncWrapper ptSync = LegacyToSpringBridge.instance(ProductTypeSyncWrapper.class);
		try {
			String rtn = ptSync.deleteProductTypes(args);
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}

	public String insertProductTypes(String jsonStr) {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		ProductTypeSyncWrapper ptSync = LegacyToSpringBridge.instance(ProductTypeSyncWrapper.class);
		try {
			String rtn = ptSync.importProductTypes(jsonStr);
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}

	public String getProductRecords(int start,int cnt) {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		ProductSyncWrapper pdSync = LegacyToSpringBridge.instance(ProductSyncWrapper.class);
		try {
			String rtn = pdSync.exportProducts(start,cnt);
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}
	
	public String getProductRecordsWithCallback(int start,int cnt,String p_callback) {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		ProductSyncWrapper pdSync = LegacyToSpringBridge.instance(ProductSyncWrapper.class);
		try {
			String rtn = pdSync.exportProducts(start,cnt,null,new StringReturnCallback() {

				@Override
				public String returnCallback(String rtn) throws Exception {
					// TODO Auto-generated method stub
					conn.callSegment(p_callback,
								new VectorUtil()
									.addElement(rtn)
									.toVector()
							);
					JSONObject jo = new JSONObject();
					return (jo.toString());
				}
				
			});
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}
	
	
	public String insertProductRecords(String jsonStr) {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		ProductSyncWrapper pdSync = LegacyToSpringBridge.instance(ProductSyncWrapper.class);
		try {
			String rtn = pdSync.importProducts(jsonStr);
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}
	public String getProductVariants(int start,int cnt) {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		ProductVariantSyncWrapper pvSync = LegacyToSpringBridge.instance(ProductVariantSyncWrapper.class);
		try {
			String rtn = pvSync.exportVariants(start,cnt);
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}
	public String getProductVariantsWithCallback(int start,int cnt,String p_callback) {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		ProductVariantSyncWrapper pvSync = LegacyToSpringBridge.instance(ProductVariantSyncWrapper.class);
		try {
			String rtn = pvSync.exportVariants(start,cnt,new StringReturnCallback() {

				@Override
				public String returnCallback(String rtn) throws Exception {
					// TODO Auto-generated method stub
					conn.callSegment(p_callback,
								new VectorUtil()
									.addElement(rtn)
									.toVector()
							);
					JSONObject jo = new JSONObject();
					return (jo.toString());
				}
				
			});
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}

	public String deleteProductVariants(Vector args) {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		ProductVariantSyncWrapper pvSync = LegacyToSpringBridge.instance(ProductVariantSyncWrapper.class);
		try {
			String rtn = pvSync.deleteVariantRecords(args);
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}

	public String insertProductVariants(String jsonStr) {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		ProductVariantSyncWrapper pvSync = LegacyToSpringBridge.instance(ProductVariantSyncWrapper.class);
		try {
			String rtn = pvSync.importVariants(jsonStr);
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}

	public String getProductMedia(int start,int cnt) {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		ProductMediaSyncWrapper pmSync = LegacyToSpringBridge.instance(ProductMediaSyncWrapper.class);
		try {
			String rtn = pmSync.exportProductMedia(start,cnt);
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}

	public String deleteProductMedia(Vector args) {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		ProductMediaSyncWrapper pmSync = LegacyToSpringBridge.instance(ProductMediaSyncWrapper.class);
		try {
			String rtn = pmSync.deleteProductMedia(args);
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}

	public String insertProductMedia(String jsonStr) {
		// TODO Auto-generated method stub
//		SchemaReader r = new SchemaReader("/saleor.graphql");
		
		ProductMediaSyncWrapper pmSync = LegacyToSpringBridge.instance(ProductMediaSyncWrapper.class);
		try {
			String rtn = pmSync.importProductMedia(jsonStr);
			return(rtn);
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("FAIL");
	}
	
	public String addMediaToProduct(String p_slug,String p_url,String p_mediaType,String p_alt) {
		SaleorMediaService sSync  = LegacyToSpringBridge.instance(SaleorMediaService.class);
		sSync.addImage(p_slug, p_url, p_mediaType,p_alt);
		return("OK");
	}

	public String dtTestRpc(String p_slug) {
		SaleorMediaService sSync  = LegacyToSpringBridge.instance(SaleorMediaService.class);
		String mediaStr = sSync.queryMediaySlug(p_slug);
		if(mediaStr != null) return("OK  "+mediaStr); else return("FAIL"+p_slug);
	}
}
