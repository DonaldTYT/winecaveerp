package com.uniinformation.zkf.winecave;

import java.io.ByteArrayOutputStream;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Filedownload;

import com.uniinformation.bicore.BiSchema;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.winecave.Winelist;
import com.uniinformation.zkf.ZkCellActionForm;

public class ZkComposerWineList  extends ZkCellActionForm   {
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
	onClickListener = new EventListener(){
		@Override
		public void onEvent(Event arg0) throws Exception {
			if(arg0.getTarget().getId().equals("btOK")) {
				String vcode =  null;
				if(formCollection.testCell("cust1") != null) {
					vcode = formCollection.getCell("cust1").getString();
					if(vcode == null || vcode.trim().equals("")) vcode = null;
				}
				BiSchema schema = BiSchema.loadSchema(sessionHelper);
				SelectUtil su = schema.getSelectUtil();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				Winelist.getFullWineList(vcode, su,bos);
				Filedownload.save(bos.toByteArray(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "winelist.xls");
				su.close();
			}
			// TODO Auto-generated method stub
			
		}
	};

		super.doAfterCompose(arg0);
	}
}
