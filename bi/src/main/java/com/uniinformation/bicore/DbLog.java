package com.uniinformation.bicore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtil.FilingUtilPropObj;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class DbLog {
	
	static public void dblogInsertOne(SessionHelper p_sh,String p_view,Object p_sid,String p_mode,JSONObject p_preImage,JSONObject p_postImage) throws Exception{
			Connection conn = null;
			try {
				FilingUtilPropObj fprop = FilingUtil.loadProp(p_sh.getAgent());
				conn = fprop.jdbcPool.getConnection();
				String insertSQL = String.format(
						"INSERT INTO dblog(dblog_agent, dblog_view, dblog_user,dblog_session,dblog_host, dblog_sid,dblog_mode,dblog_version, dblog_time, dblog_preimage,dblog_postimage) VALUES(?,?,?,?,?,?,?,?,?,?,?)");
				PreparedStatement pstmt = conn.prepareStatement(insertSQL);
				pstmt.setString(1, p_sh.getAgent());
				pstmt.setString(2, p_view);
				pstmt.setString(3, p_sh.getLoginId());
				pstmt.setString(4, "");
				pstmt.setString(5, "");
				pstmt.setObject(6, p_sid);
				pstmt.setString(7, p_mode);
				pstmt.setInt(8, 0);
				pstmt.setTimestamp(9, new Timestamp(new Date().getTime()));
				if(p_preImage != null) pstmt.setBinaryStream(10, new ByteArrayInputStream(p_preImage.toString().getBytes("UTF-8"))); else pstmt.setString(10, "");
				if(p_postImage != null) pstmt.setBinaryStream(11, new ByteArrayInputStream(p_postImage.toString().getBytes("UTF-8"))); else pstmt.setString(11, "");
				pstmt.executeUpdate();
			}
			catch(Exception ex){
				throw(ex);
			}
			finally{
				try{
					if (conn != null) conn.close();
					conn = null;
				} catch (Exception ex){ ex.printStackTrace(); }
			}
	}
}