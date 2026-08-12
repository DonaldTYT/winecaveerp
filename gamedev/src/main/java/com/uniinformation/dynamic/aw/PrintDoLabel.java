package com.uniinformation.dynamic.aw;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.prtdoc.PrtdocInterface;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class PrintDoLabel extends BiActionHandler implements JxActionListener {
//	HashSet<Integer> cfmSet;
	String zpl;
	public PrintDoLabel() {
		super(null);
	}
	public PrintDoLabel(ZkBiComposerBase p_bibase) {
		super(p_bibase);
	}
	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
//		cfmSet = new HashSet<Integer>();
		zpl = null;
		return (ReturnMsg.defaultOk);
	}
	
	ReturnMsg confirmOneQuotation(BiResult p_result, BiResult quoBr,int invrg) {
		return(ReturnMsg.defaultOk);
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		return (ReturnMsg.defaultOk);
	}
	@Override
	public ReturnMsg afterAction(BiResult p_br) {
		if(zpl != null) {
			biBase.biBaseRefresh(p_br);
		}
		return (ReturnMsg.defaultOk);
	}
	@Override
	public void actionPerformed(JxField field) {
		
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		BiResult br = jxf.getBr();
		zpl = buildZpl(
				String.format("https://www.erpv4.com/bctag?agent=aw&dnno=%s", br.getCellString("stm_ref1")),
				br.getCellString("stm_ref1")+" "+ br.getCellString("stm_allorders")
				);
		/*
		Messagebox.show("Confirm Order ?", "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
			new EventListener() {
			   public void onEvent(Event evt) throws Exception {
			    	if (((Integer)evt.getData()) == Messagebox.YES){
			    		BiResult quoBr;
			    		quoBr = br.getSessionHelper().getBiSchema().getViewByName("erpv4.QuotationG2").newBiResult(br.getSessionHelper().getLoginId(), null, null, br.getSessionHelper());
			    		ReturnMsg rtn = confirmOneQuotation(br, quoBr,invrg);
			    		if(rtn != null && !rtn.getStatus()) {
			    			field.getJxForm().messageBox("Error while confirm quotation " + rtn.getMsg());
			    		} else {
			    			((JxZkBiBase) jxf).refreshAllListitem();
			    		}
			    	} else{
			    		return;
			    	}
			   }
			}
		);
		*/
		askLabelPrinterToPrint() ;
		
	}
	
	void askLabelPrinterToPrint() {
		if(zpl == null) return;
		Messagebox.show(
			    "Please choose printer:",
			    "Select",
			    new Messagebox.Button[] {
			        Messagebox.Button.YES,
			        Messagebox.Button.NO,
			        Messagebox.Button.RETRY,
			        Messagebox.Button.CANCEL
			    },
			    new String[] {
			        "P1",
			        "P2",
			        "P3",
			        "Cancel"
			    },
			    Messagebox.QUESTION,
			    Messagebox.Button.CANCEL,
			    event -> {
			        Messagebox.Button button = event.getButton();

			        if (button == Messagebox.Button.YES) {
//			        	sendToPrinter("192.168.46.102", 9100, zpl);
			        	sendToPrinter("192.168.11.245", 9100, zpl);
			            // user clicked "1"
			        } else if (button == Messagebox.Button.NO) {
			        	sendToPrinter("192.168.11.246", 9100, zpl);
			            // user clicked "2"
			        } else if (button == Messagebox.Button.RETRY) {
			        	sendToPrinter("192.168.11.247", 9100, zpl);
			            // user clicked "3"
			        } else if (button == Messagebox.Button.CANCEL) {
			            // user clicked Cancel
			        }
			    }
			);
	}

	@Override
	public boolean isVisible(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(false);
//		return(p_br.getSessionHelper().hasAccessRight("#cfmwo"));
		return(true);
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		if(p_isBatch) {
			return(false);
		} else {
			if(StringUtils.isBlank(p_br.getCellString("stm_ref1"))) {
				return(true);
			}
			if(p_br.inBeginWork()) return(true);
			/*
			String qs = p_br.getCellString("inv_quostatus");
			if(qs.equals("Confirmed") || qs.equals("Void")) return(true);
			*/
			return(false);
		}
	}
	
    /*
     * Zebra ZD411 typical 203 dpi:
     * 40mm x 30mm label ~= 320 x 240 dots
     *
     * Usage:
     *   java ZebraQrPrint <printer-ip> <port> <qr-value> <comment>
     *
     * Example:
     *   java ZebraQrPrint 192.168.11.245 9100 \
     *   "https://hub.erpv4.com/bicore/devredir.html?agent=aw&dnno=DN25080013" \
     *   "DN25080013 Q25070917,Q25070918"
     */

    private static final int DPI = 203;
    private static final double DOTS_PER_MM = DPI / 25.4;

    private static final int LABEL_WIDTH_MM = 40;
    private static final int LABEL_HEIGHT_MM = 30;

    private static final int CONNECT_TIMEOUT_MS = 3000;

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage:");
            System.err.println("  java ZebraQrPrint <printer-ip> <port> <qr-value> <comment>");
            System.err.println();
            System.err.println("Example:");
            System.err.println("  java ZebraQrPrint 192.168.11.245 9100 \"DN25080013\" \"DN25080013 Q25070917,Q25070918\"");
            System.exit(1);
        }

        String printerIp = args[0];
        int port = Integer.parseInt(args[1]);
        String qrValue = args[2];
        String comment = args[3];

        try {
            String zpl = buildZpl(qrValue, comment);
            sendToPrinter(printerIp, port, zpl);
            System.out.println("Print job sent successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static String buildZpl(String qrValue, String comment) {
        int labelWidthDots = mmToDots(LABEL_WIDTH_MM);    // about 320
        int labelHeightDots = mmToDots(LABEL_HEIGHT_MM);  // about 240

        /*
         * QR layout for 40mm x 30mm label.
         *
         * For short QR value, magnification 4 is good.
         * For long URL, magnification 4 should still fit in many cases,
         * but if it becomes too large, change qrMagnification to 3.
         */
        int qrMagnification = 4;

        /*
         * Approximate QR placement.
         * Zebra QR actual size depends on data length.
         * This position is tuned for 40x30mm label.
         */
        int qrX = 92;
        int qrY = 8;

        /*
         * Bottom comment:
         * Small font, centered, max 2 rows, auto-wrap.
         */
        int commentX = 5;
        int commentY = 180;
        int commentWidth = labelWidthDots - 10;

        int fontHeight = 16;
        int fontWidth = 16;

        String safeQrValue = zplHexEncode(qrValue);
        String safeComment = zplHexEncode(comment);

        StringBuilder zpl = new StringBuilder();

        zpl.append("^XA\n");

        // UTF-8 mode. Useful for newer Zebra Link-OS printers.
        // For simple English/numbers, it is also safe.
        zpl.append("^CI28\n");

        // Label size
        zpl.append("^PW").append(labelWidthDots).append("\n");
        zpl.append("^LL").append(labelHeightDots).append("\n");
        zpl.append("^LH0,0\n");

        // Optional print settings
        // zpl.append("^PR3\n");    // print speed
        // zpl.append("^MD10\n");   // darkness

        /*
         * QR Code:
         * ^BQN,2,4
         *   2 = QR model 2
         *   4 = magnification
         *
         * ^FDLA,<data>
         *   L = error correction level L
         *   A = automatic input mode
         */
        zpl.append("^FO").append(qrX).append(",").append(qrY).append("\n");
        zpl.append("^BQN,2,").append(qrMagnification).append("\n");
        zpl.append("^FH^FDLA,").append(safeQrValue).append("^FS\n");

        /*
         * Text comment:
         * ^FB<width>,2,1,C,0
         *   width = field block width
         *   2     = max 2 lines
         *   1     = line spacing
         *   C     = centered
         */
        zpl.append("^FO").append(commentX).append(",").append(commentY).append("\n");
        zpl.append("^A0N,").append(fontHeight).append(",").append(fontWidth).append("\n");
        zpl.append("^FB").append(commentWidth).append(",2,1,C,0\n");
        zpl.append("^FH^FD").append(safeComment).append("^FS\n");

        zpl.append("^XZ\n");

        return zpl.toString();
    }

    private static void sendToPrinter(String ip, int port, String zpl) throws Exception {
        Socket socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(ip, port), CONNECT_TIMEOUT_MS);

            OutputStream out = socket.getOutputStream();
            out.write(zpl.getBytes(StandardCharsets.US_ASCII));
            out.flush();

            /*
             * Do not wait for response.
             * Normal ZPL print jobs usually do not return data.
             */
        } finally {
            socket.close();
        }
    }

    private static int mmToDots(int mm) {
        return (int) Math.round(mm * DOTS_PER_MM);
    }

    /*
     * ZPL-safe text encoding for ^FH mode.
     *
     * Special ZPL characters are escaped:
     *   ^  -> _5E
     *   ~  -> _7E
     *   _  -> _5F
     *
     * Non-ASCII UTF-8 bytes are also escaped as _XX.
     */
    private static String zplHexEncode(String value) {
        if (value == null) {
            return "";
        }

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            int c = b & 0xFF;

            // Safe printable ASCII, except ZPL special chars and underscore used by ^FH
            if (c >= 32 && c <= 126 && c != '^' && c != '~' && c != '_') {
                sb.append((char) c);
            } else {
                sb.append('_');
                String hex = Integer.toHexString(c).toUpperCase();
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
        }

        return sb.toString();
    }
}
