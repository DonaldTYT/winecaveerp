package com.uniinformation.zkf;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.commons.io.input.ReaderInputStream;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.util.media.Media;
import org.zkoss.zul.Fileupload;

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
					Fileupload.get(
						new HashMap<String, Object>(),
						null,
						null,
						-1,
						-1,
						true,
						new ZkBiEventListener <UploadEvent>(){
						@Override
			    		public void onZkBiEvent(UploadEvent event) throws Exception {
			        		UniLog.log("translate template upload event catched");
			        		Media media = event.getMedia();
			        		if (media != null) {
			        			try (InputStream inputStream = openInputStream(media)) {
			        				processUploadedFile(inputStream);
			        			}
			        		}
			    		}
			    	});
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
