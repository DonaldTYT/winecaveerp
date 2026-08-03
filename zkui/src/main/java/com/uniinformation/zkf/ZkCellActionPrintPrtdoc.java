package com.uniinformation.zkf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.commons.io.input.ReaderInputStream;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.util.media.Media;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Fileupload;

import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiEventListener;

public class ZkCellActionPrintPrtdoc extends ZkCellActionForm {
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		onClickListener = new EventListener() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				Component c = (Component)arg0.getTarget();
				if(c.getId().equals("btOK")) {
					String fname = formCollection.getCellString("prtdocUrl");
					InputStream is = sessionHelper.newErpFileInputStream(fname);
					ChnftrParser ps = new ChnftrParser(is,"");
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ps.print(bos);
					ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
					Filedownload.save(bis, "application/pdf", "Document"+ ".pdf");
				}
			}
		};
		super.doAfterCompose(arg0);
	}

	private InputStream openInputStream(Media media) {
		if (media.isBinary()) {
			return media.getStreamData();
		}

		return new ReaderInputStream(media.getReaderData(), StandardCharsets.UTF_8);
	}

	/**
	 * Processes the uploaded file while its input stream is open.
	 */
	protected void processUploadedFile(InputStream inputStream) throws Exception {
		// Add further processing here.
	}

}
