package com.uniinformation.zkbi.propertymgmt;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkcomp.ZkBiButton;
import static com.uniinformation.utils.ZkUtil.throwFunction;

public class ZkBiComposerRenovationDeposit extends ZkBiComposerBase {

	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
   		super.doAfterCompose(comp);
		Selectors.find("[id^='btExtraBatchAction_']").stream().map(x -> (Button)x).forEach(bt -> {
			bt.addSclass("orange1");
		});
   	}

	@Override
    protected JxZkBiBase buildDetailWindow(final BiResult result) {
		try {
			return super.buildDetailWindow(result);
		} finally {
			Selectors.find("[id^='btExtraJxFormAction_']").stream().map(x -> (Button)x).forEach(bt -> {
				bt.addSclass("orange1");
			});
		}
    }

	@Override
    protected void setupExtraButton(final BiResult result) {
    	super.setupExtraButton(result);
    	if (sessionHelper.hasAccessRight("#pmgtadm1")) {
    		String cocode = Erpv4Config.getDefaultCoCode(sessionHelper);
	       	Button btn = new ZkBiButton("按金單內容", null, "btOverduePaymentContent");
	       	btn.addSclass("lightgreen1");
	       	btn.addEventListener(Events.ON_CLICK, event -> {
	       		Textbox tb = new Textbox();
	       		tb.setHflex("1");
	       		tb.setVflex("1");
	       		tb.setStyle("resize:none;font-size: 12pt !important;line-height:18pt");
	       		tb.setMultiline(true);
	       		tb.setText(ZkUtil.getFirstTableRec(result.getSelectUtil(), 
	       							"select co_rendepcontent from cocode where co_cocode = ?", new Wherecl().appendArgument(cocode))
	       						.map(throwFunction(tr -> tr.getFieldString("co_rendepcontent"))).orElse(""));
	       		Vlayout vl = new Vlayout();
	       		vl.setHeight("calc(80vh - 120px)");
	       		vl.appendChild(tb);
	       		ZkBiMsgbox.build2(vl, 10, 785.2, (ev, mbbt) -> {
	       			if (mbbt.getIdx() == 0) {
	       				ZkUtil.importAction.accept(sessionHelper, su -> {
	       					su.executeUpdate("update cocode set co_rendepcontent = ? where co_cocode = ?", new Wherecl().appendArgument(tb.getText()).appendArgument(cocode));
	       				});
	       				ZkUtil.showMsg("更新完成");
	       			}
	       		}, "Ok", "Cancel").setTitle(btn.getLabel()).doModal();
	       	});
	        abHelper.addButton(btn);
    	}
    }
}
