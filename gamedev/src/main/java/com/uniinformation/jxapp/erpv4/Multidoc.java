package com.uniinformation.jxapp.erpv4;

import org.zkoss.zul.Messagebox;

import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ZkUtil;

public class Multidoc extends JxZkBiBase{
	@Override
	public void afterBind() {
		super.afterBind();
		new JxFieldAction("btf_download") {
			@Override
			public void actionPerformed(JxField jxfield) {
				String fn = getBr().getCellString("mdoc_filekey");
				if(getBr().getCellString("mdoc_doctype").equals("application/pdf")) {
					fn += ".pdf";
				}
				ZkUtil.downloadFileFromFiling(getSessionHelper(), getBr().getCellString("mdoc_doctype"), getBr().getCellString("mdoc_filekey"), fn);
				Messagebox.show("Document Downloaded as " + fn);
			}
		};
	}
}
