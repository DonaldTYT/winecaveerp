package com.uniinformation.dynamic.winecave;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;

import com.kikyosoft.ai.comfy.ComfyUIImageToTextClient;
import com.kikyosoft.utils.EuropeanTextNormalizer;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.wc.BiResultPhotoHeader;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;

/**
 * Generates an image description for each selected Photo Header record and
 * saves the returned text to the AI Comments column.
 */
public class GeneratePhotoAIComments extends BiActionHandler {

    private static final String PHOTO_ID_COLUMN = "pdpi_photoid";
    private static final String AI_COMMENTS_COLUMN = "pdpi_comments";
    private static final String DEFAULT_WORKFLOW_FILE = "c:/tmp/imageToText_api_Q4_K_M_Instruct.json";

    private final File workflowFile;
    private final String defaultPromptText;

    private BiResultPhotoHeader br;
    private ComfyUIImageToTextClient comfyClient;
    private String promptText;

    public GeneratePhotoAIComments(ZkBiComposerBase p_bibase) {
        super(p_bibase);
        useAsync = p_bibase != null
                && p_bibase.getSessionHelper().getAllowBatchPrtdocAsync();
        workflowFile = new File(System.getProperty(
                "comfy.imageToText.workflow",
                DEFAULT_WORKFLOW_FILE));
        defaultPromptText = ComfyUIImageToTextClient.resolveDefaultPromptText();
    }

    @Override
    public ReturnMsg beforeAction(BiResult p_result, int cnt) {
		delayStart = false;
        if (!(p_result instanceof BiResultPhotoHeader)) {
            return new ReturnMsg(false,
                    "Generate Photo AI Comments requires graphql.photohdr");
        }
        if (!workflowFile.isFile() || !workflowFile.canRead()) {
            return new ReturnMsg(false,
                    "ComfyUI workflow file is not readable: " + workflowFile.getAbsolutePath());
        }

		delayStart = true;
        ReturnMsg dialogResult = showPromptDialog((BiResultPhotoHeader) p_result);
        if (dialogResult != null && !dialogResult.getStatus()) {
            delayAbort();
            return dialogResult;
        }
        return ReturnMsg.defaultOk;
    }

    private ReturnMsg showPromptDialog(final BiResultPhotoHeader result) {
        final Textbox promptTextbox = new Textbox();
        promptTextbox.setValue(defaultPromptText);
        promptTextbox.setMultiline(true);
        promptTextbox.setRows(6);
        promptTextbox.setWidth("500px");

        final Vbox content = new Vbox();
        content.setHflex("1");
        content.appendChild(new Label(
                biBase.getSessionHelper().getLabel("Image description prompt")));
        content.appendChild(promptTextbox);

        final AtomicBoolean dialogResolved = new AtomicBoolean(false);
        try {
            ZkBiMsgbox promptDialog = new ZkBiMsgbox(biBase.getSessionHelper())
                    .setContent(content)
                    .setButtons(new String[] {"Proceed", "Cancel"})
                    .setEventListener(new ZkBiEventListener<Event>() {
                        @Override
                        public void onZkBiEvent(Event event) throws Exception {
                            ZkBiMsgboxButton button = (ZkBiMsgboxButton) event.getTarget();
                            if (button.getIdx() == 0) {
                                if (dialogResolved.compareAndSet(false, true)) {
                                    promptText = ComfyUIImageToTextClient.resolvePromptText(
                                            promptTextbox.getValue());
                                    br = result;
                                    comfyClient = new ComfyUIImageToTextClient();
                                    delayStart();
                                }
                            } else if (dialogResolved.compareAndSet(false, true)) {
                                delayAbort();
                            }
                        }
                    })
                    .build();
            promptDialog.setTitle(
                    biBase.getSessionHelper().getLabel("Generate AI Comment"));
            promptDialog.setCloseWinCallback(event -> {
                if (dialogResolved.compareAndSet(false, true)) {
                    delayAbort();
                }
            });
            promptDialog.doModal();
            promptTextbox.setFocus(true);
            return ReturnMsg.defaultOk;
        } catch (Exception ex) {
            UniLog.log(ex);
            return new ReturnMsg(false, ex.toString());
        }
    }

    @Override
    public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
        try {
            br.fetchOneRecV(p_recIdx);

            int photoId = br.getCell(PHOTO_ID_COLUMN).getInt();
            if (photoId <= 0) {
                return new ReturnMsg(false,
                        "Photo Id is missing for selected record at index " + p_recIdx);
            }

            String imageResource = String.format(
                    "message://STOCK_IMAGE/%d/jpg",
                    photoId);
            String imageFileName = "photo-" + photoId + ".jpg";

            String description;
            try (InputStream imageStream = br.getSessionHelper()
                    .newErpFileInputStream(imageResource)) {
                if (imageStream == null) {
                    return new ReturnMsg(false,
                            "Photo Image was not found for Photo Id " + photoId);
                }
                description = comfyClient.runWorkflow(
                        workflowFile,
                        imageStream,
                        imageFileName,
                        promptText);
            }

            if (description == null || description.trim().isEmpty()) {
                return new ReturnMsg(false,
                        "ComfyUI returned an empty description for Photo Id " + photoId);
            }
            description = EuropeanTextNormalizer.toEnglishAscii(description);
            br.getCell(AI_COMMENTS_COLUMN).set(description);
            ReturnMsg updateResult = br.updateCurrent();
            if (updateResult != null && !updateResult.getStatus()) {
                return updateResult;
            }

            return ReturnMsg.defaultOk;
        } catch (Exception ex) {
            UniLog.log(ex);
            return new ReturnMsg(false, ex.toString());
        }
    }

    @Override
    public ReturnMsg afterAction(BiResult p_result) {
        if (biBase != null && br != null) {
            biBase.biBaseRefresh(br);
        }
        return ReturnMsg.defaultOk;
    }

    @Override
    public void afterActionAsync(BiActionHandler.AfterActionCallback cb) {
        ReturnMsg result = afterAction(br);
        if (biBase != null) {
            biBase.hideProgressPanel();
        }
        cb.callback(result);
    }

    @Override
    public boolean isVisible(BiResult p_result, boolean p_isBatch) {
        return p_result instanceof BiResultPhotoHeader;
    }

    @Override
    public boolean isDisabled(BiResult p_result, boolean p_isBatch) {
        return !(p_result instanceof BiResultPhotoHeader) || p_result.inBeginWork();
    }

    @Override
    public ReturnMsg isRunnable(BiResult p_result, boolean p_isBatch) {
        if (!(p_result instanceof BiResultPhotoHeader)) {
            return new ReturnMsg(false,
                    "Generate Photo AI Comments requires graphql.photohdr");
        }
        return ReturnMsg.defaultOk;
    }

    @Override
    public boolean preserveListOrder() {
        return true;
    }
}
