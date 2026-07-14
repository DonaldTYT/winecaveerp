package com.uniinformation.bicore.propertymgmt;

import java.util.Objects;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import static com.uniinformation.utils.BiUtil.safeRunnable;

public class BiResultDevLogin extends BiResultPropertyMgmt {

	public BiResultDevLogin(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
	}

	@Override
	public void beforeBind() {
		super.beforeBind();
		safeRunnable(() -> {
			if (getCurrentSids() == null)
				getCell("vcol_printpagecnt").set("2");
			else
				getCell("vcol_printpagecnt").set(getCellString("ldv_printpagecnt"));
		}).run();
	}

	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col, boolean isUpdate) {
		try {
			String url = null;
			switch (col.getString("ldv_type")) {
			case "PMS MiniPOS":
				url = "propmgmt_padpayment";
				break;
			case "PMS Kiosk":
				url = "propmgmt_epayment";
				break;
			}
			if (url == null)
				throw new Exception("Wrong type");
			url += ".html?deviceid=" + col.getString("ldv_login");
			BiResult br = BiResultHelper.create(sh, "WebMenu", String.format("webm_url = '%s'", url), -1, null);
			BiResult sru = br.getSubLink("WebMenuUser");
			ReturnMsg rtn;
			boolean foundRec = br.next(false);
			BiCellCollection colu = foundRec ? sru.getRowCollectionList().stream().filter(c -> Objects.equals(c.getString("webmu_user"), "dev#0000")).findFirst().orElse(null) : null;
			if (colu == null) {
				colu = sru.newRowCollection();
				colu.getCell("webmu_user").set("dev#0000");
				colu.getCell("webmu_active").set(true);
				rtn = sru.addSubRecord(colu, "");
				if (!rtn.getStatus())
					return rtn;
			}
			if (!foundRec) {
				br.getCell("webm_desc").set(url.startsWith("propmgmt_padpayment") ? "PadPayment" : "Epayment");
				br.getCell("webm_url").set(url);
				rtn = br.addCurrent();
			} else
				rtn = br.updateCurrent();
			return rtn;
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.toString());
		}
	}
}
