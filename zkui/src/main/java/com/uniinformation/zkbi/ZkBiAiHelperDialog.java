package com.uniinformation.zkbi;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

/**
 * Provider-neutral conversational UI for the ZK BI page helper.
 */
public final class ZkBiAiHelperDialog {
    private static final int MAX_HISTORY_TURNS = 10;
    private static final String SYSTEM_PROMPT =
            "You are the in-application AI help assistant for a business data page. "
          + "Help the user understand and operate the current list and record-detail page. "
          + "For questions about this page, call get_page_context before answering. "
          + "For instructions about how to operate the page, call list_page_operations and then "
          + "get_operation_help for the relevant operation. Follow the returned interaction and "
          + "control behavior exactly; do not invent an Enter key, search button, click, delay, "
          + "or other generic web behavior. Base page-specific claims only on tool results. "
          + "Explain the returned steps clearly and concisely. Available operations depend on the "
          + "current view, page state and logged-in user's permissions. "
          + "Never claim that you performed an action or changed data. If a requested operation is "
          + "not supported by the supplied tools, say that exact instructions are not available. "
          + "If an operation tool fails, do not fill the gap with generic UI advice or guessed "
          + "syntax. In particular, do not suggest SQL percent patterns, wildcard syntax, keyboard "
          + "actions or controls unless the successful tool result explicitly documents them. "
          + "Do not request passwords or API keys.";

    private final ZkBiAiHelperContext invoker;
    private final ZkBiAiHelperAgent agent;
    private final SessionHelper sessionHelper;
    private final Component parentComp;
    private final boolean mobile;
    private final List<Turn> history = new ArrayList<Turn>();
    private final List<ZkBiAiHelperAgent.Tool> tools;

    private Window dialog;
    private Div transcript;
    private Textbox question;
    private Button sendButton;

    public ZkBiAiHelperDialog(ZkBiAiHelperContext invoker, ZkBiAiHelperAgent agent) {
        if (invoker == null)
            throw new IllegalArgumentException("invoker must not be null");
        if (agent == null)
            throw new IllegalArgumentException("agent must not be null");
        this.invoker = invoker;
        this.agent = agent;
        this.sessionHelper = invoker.getAiHelpSessionHelper();
        this.parentComp = invoker.getAiHelpParentComponent();
        this.mobile = sessionHelper.isMobileDevice();
        this.tools = new ArrayList<ZkBiAiHelperAgent.Tool>();
        this.tools.add(new PageContextTool());
        this.tools.add(new PageOperationsTool());
        this.tools.add(new OperationHelpTool());
    }

    public void show() {
        if (dialog == null || dialog.getPage() == null)
            buildDialog();
        dialog.doHighlighted();
        if (mobile)
            installMobileViewportHandler();
        question.focus();
    }

    private void buildDialog() {
        dialog = new Window(sessionHelper.getLabel("AI Help"), "normal", true);
        dialog.setClosable(true);
        dialog.setSizable(!mobile);
        dialog.setSclass("zkbi-ai-helper-dialog" + (mobile ? " zkbi-ai-helper-dialog-mobile" : ""));
        dialog.setWidth(mobile ? "100%" : "90%");
        dialog.setHeight(mobile ? "100%" : "85%");
        dialog.setStyle(mobile
                ? "position:fixed!important;left:0!important;top:0!important;"
                        + "max-width:none;max-height:none;margin:0;border-radius:0;"
                : "max-width:1100px;max-height:900px;");
        dialog.setParent(parentComp);

        Vlayout root = new Vlayout();
        root.setHflex("1");
        root.setVflex("1");
        root.setSclass("zkbi-ai-helper-layout");
        root.setStyle("box-sizing:border-box;min-height:0;overflow:hidden;"
                + (mobile ? "padding:6px;" : "padding:12px;"));
        root.setParent(dialog);

        transcript = new Div();
        transcript.setHflex("1");
        transcript.setVflex("1");
        transcript.setSclass("zkbi-ai-helper-transcript");
        transcript.setStyle("box-sizing:border-box;min-height:0;overflow-x:hidden;overflow-y:auto;"
                + "overscroll-behavior:contain;-webkit-overflow-scrolling:touch;"
                + "border:1px solid #d9d9d9;background:#fafafa;"
                + (mobile ? "padding:6px;" : "padding:10px;"));
        transcript.setParent(root);
        appendMessage("AI", sessionHelper.getLabel(
                "Ask me how to use this page, its fields, or its available record actions."), false);

        question = new Textbox();
        question.setMultiline(true);
        question.setRows(mobile ? 2 : 3);
        question.setHflex("1");
        question.setSclass("zkbi-ai-helper-question");
        question.setStyle("box-sizing:border-box;resize:none;line-height:1.35;"
                + (mobile ? "min-height:64px;font-size:16px;padding:9px;" : "min-height:76px;"));
        question.setPlaceholder(sessionHelper.getLabel("Ask a question about this page"));
        question.setParent(root);

        Div actions = new Div();
        actions.setWidth("100%");
        actions.setSclass("zkbi-ai-helper-actions");
        actions.setStyle(mobile
                ? "box-sizing:border-box;display:grid;grid-template-columns:minmax(0,1fr) minmax(0,1fr);"
                        + "align-items:center;gap:8px;width:100%;overflow:hidden;"
                : "display:flex;justify-content:flex-end;align-items:center;gap:8px;");
        actions.setParent(root);

        Button clearButton = new Button(sessionHelper.getBtLabel("Clear"));
        clearButton.setSclass("zkbi-ai-helper-clear");
        clearButton.setStyle(mobile
                ? "box-sizing:border-box;width:100%;min-width:0;min-height:44px;font-size:15px;"
                : "min-width:90px;");
        clearButton.setParent(actions);
        clearButton.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
            @Override
            public void onZkBiEvent(Event event) {
                history.clear();
                transcript.getChildren().clear();
                appendMessage("AI", sessionHelper.getLabel(
                        "Conversation cleared. What would you like to know about this page?"), false);
            }
        });

        sendButton = new Button(sessionHelper.getBtLabel("Ask"));
        sendButton.setSclass("z-button-primary zkbi-ai-helper-send");
        sendButton.setStyle(mobile
                ? "box-sizing:border-box;width:100%;min-width:0;min-height:44px;font-size:15px;"
                : "min-width:110px;");
        sendButton.setParent(actions);
        sendButton.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
            @Override
            public void onZkBiEvent(Event event) {
                sendQuestion();
            }
        });
    }

    /**
     * Keep the highlighted window aligned with the browser's actual visual viewport.
     * ZK's server-side ClientInfoEvent can contain an intermediate/stale orientation
     * size on iOS, especially after several rotations, so this must run in the browser.
     */
    private void installMobileViewportHandler() {
        if (!mobile || dialog == null)
            return;
        String id = dialog.getUuid();
        Clients.evalJavaScript(
                "(function(){"
              + "var id='" + id + "',key='zkbi_'+id;"
              + "var all=window.__zkBiAiViewportHandlers||(window.__zkBiAiViewportHandlers={});"
              + "if(all[key])all[key].remove();"
              + "var vv=window.visualViewport,seen=false,timers=[];"
              + "var remove=function(){"
              + "window.removeEventListener('resize',schedule);"
              + "window.removeEventListener('orientationchange',rotate);"
              + "if(vv){vv.removeEventListener('resize',schedule);vv.removeEventListener('scroll',schedule);}"
              + "for(var i=0;i<timers.length;i++)window.clearTimeout(timers[i]);"
              + "delete all[key];};"
              + "var update=function(){"
              + "var n=document.getElementById(id);"
              + "if(!n){if(seen)remove();return;}seen=true;"
              + "var current=window.visualViewport;"
              + "var w=Math.round(current?current.width:document.documentElement.clientWidth);"
              + "var h=Math.round(current?current.height:document.documentElement.clientHeight);"
              + "var l=Math.round(current?current.offsetLeft:0);"
              + "var t=Math.round(current?current.offsetTop:0);"
              + "if(w<1||h<1)return;"
              + "n.style.setProperty('position','fixed','important');"
              + "n.style.setProperty('left',l+'px','important');"
              + "n.style.setProperty('top',t+'px','important');"
              + "n.style.setProperty('width',w+'px','important');"
              + "n.style.setProperty('height',h+'px','important');"
              + "n.style.setProperty('max-width','none','important');"
              + "n.style.setProperty('max-height','none','important');"
              + "var widget=window.zk&&zk.Widget?zk.Widget.$(n):null;"
              + "if(widget&&window.zUtl)window.requestAnimationFrame(function(){zUtl.fireSized(widget);});};"
              + "var schedule=function(){timers.push(window.setTimeout(update,0));};"
              + "var rotate=function(){schedule();timers.push(window.setTimeout(update,80));"
              + "timers.push(window.setTimeout(update,220));timers.push(window.setTimeout(update,500));};"
              + "all[key]={remove:remove};"
              + "window.addEventListener('resize',schedule,{passive:true});"
              + "window.addEventListener('orientationchange',rotate,{passive:true});"
              + "if(vv){vv.addEventListener('resize',schedule,{passive:true});"
              + "vv.addEventListener('scroll',schedule,{passive:true});}"
              + "rotate();"
              + "})();");
    }

    private void sendQuestion() {
        final String userQuestion = StringUtils.trimToEmpty(question.getValue());
        if (userQuestion.isEmpty()) {
            question.focus();
            return;
        }

        question.setValue("");
        appendMessage("You", userQuestion, true);
        setInputEnabled(false);

        new ZkBiAbstractLongOp(dialog, sessionHelper.getLabel("Asking AI..."), 50) {
            @Override
            public ReturnMsg longOp() {
                try {
                    String reply = agent.chat(SYSTEM_PROMPT, buildConversation(userQuestion), tools);
                    if (StringUtils.isBlank(reply))
                        return new ReturnMsg(false, sessionHelper.getLabel("The AI returned an empty response"));
                    history.add(new Turn(userQuestion, reply));
                    trimHistory();
                    return new ReturnMsg(true, reply);
                }
                catch (Exception ex) {
                    return new ReturnMsg(false, friendlyError(ex));
                }
            }

            @Override
            public void afterLongOp(ReturnMsg rtnMsg) {
                super.afterLongOp(rtnMsg);
                appendMessage("AI", rtnMsg == null
                        ? sessionHelper.getLabel("AI Help is unavailable")
                        : rtnMsg.getMsg(), false);
                setInputEnabled(true);
                question.focus();
            }
        };
    }

    private String buildConversation(String currentQuestion) {
        StringBuilder prompt = new StringBuilder();
        if (!history.isEmpty()) {
            prompt.append("Conversation so far:\n");
            for (Turn turn : history) {
                prompt.append("User: ").append(turn.question).append('\n');
                prompt.append("Assistant: ").append(turn.answer).append("\n\n");
            }
        }
        return prompt.append("Current user question: ").append(currentQuestion).toString();
    }

    private void appendMessage(String speaker, String message, boolean userMessage) {
        Div bubble = new Div();
        bubble.setSclass("zkbi-ai-helper-message "
                + (userMessage ? "zkbi-ai-helper-message-user" : "zkbi-ai-helper-message-ai"));
        bubble.setStyle("box-sizing:border-box;margin:6px 0;padding:9px 11px;border-radius:8px;"
                + "white-space:pre-wrap;overflow-wrap:anywhere;word-break:break-word;line-height:1.4;"
                + (userMessage
                        ? "background:#e8f2ff;margin-left:" + (mobile ? "4%;" : "12%;")
                        : "background:#fff;border:1px solid #e2e2e2;margin-right:"
                                + (mobile ? "4%;" : "12%;")));
        Label label = new Label(speaker + ":\n" + StringUtils.defaultString(message));
        label.setMultiline(true);
        label.setPre(true);
        label.setStyle("max-width:100%;white-space:pre-wrap;overflow-wrap:anywhere;word-break:break-word;"
                + (mobile ? "font-size:14px;" : ""));
        label.setParent(bubble);
        bubble.setParent(transcript);
        if (transcript.getChildren().size() > 1 && dialog != null
                && dialog.getPage() != null && dialog.isVisible())
            Clients.scrollIntoView(bubble);
    }

    private void setInputEnabled(boolean enabled) {
        question.setDisabled(!enabled);
        sendButton.setDisabled(!enabled);
    }

    private void trimHistory() {
        while (history.size() > MAX_HISTORY_TURNS)
            history.remove(0);
    }

    private String friendlyError(Exception ex) {
        String message = StringUtils.defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName());
        if (message.contains("OPENAI_API_KEY"))
            return sessionHelper.getLabel("AI Help is not configured: OPENAI_API_KEY is not set");
        return sessionHelper.getLabel("AI Help request failed") + ": " + message;
    }

    private final class PageContextTool implements ZkBiAiHelperAgent.Tool {
        @Override
        public String getName() {
            return "get_page_context";
        }

        @Override
        public String getDescription() {
            return "Returns live, read-only metadata for the invoking BI page, including its current "
                    + "list/detail state, permissions and fields. It never returns record values.";
        }

        @Override
        public JSONObject getParameters() throws JSONException {
            return new JSONObject()
                    .put("type", "object")
                    .put("properties", new JSONObject())
                    .put("additionalProperties", false);
        }

        @Override
        public Object execute(JSONObject arguments) throws JSONException {
            return invoker.getAiHelpContext();
        }
    }

    private final class PageOperationsTool implements ZkBiAiHelperAgent.Tool {
        @Override
        public String getName() {
            return "list_page_operations";
        }

        @Override
        public String getDescription() {
            return "Lists operations for which the invoking BI page can provide exact, "
                    + "permission-aware operating instructions. It does not perform an operation.";
        }

        @Override
        public JSONObject getParameters() throws JSONException {
            return new JSONObject()
                    .put("type", "object")
                    .put("properties", new JSONObject())
                    .put("additionalProperties", false);
        }

        @Override
        public Object execute(JSONObject arguments) throws Exception {
            try {
                return invoker.getAiHelpOperationCatalog();
            }
            catch (Exception ex) {
                UniLog.log(ex);
                throw ex;
            }
        }
    }

    private final class OperationHelpTool implements ZkBiAiHelperAgent.Tool {
        @Override
        public String getName() {
            return "get_operation_help";
        }

        @Override
        public String getDescription() {
            return "Returns exact controls, triggers and steps for one supported operation in the "
                    + "current BI page state. It is read-only and never changes records.";
        }

        @Override
        public JSONObject getParameters() throws JSONException {
            JSONObject operationId = new JSONObject()
                    .put("type", "string")
                    .put("description", "Operation id returned by list_page_operations");
            return new JSONObject()
                    .put("type", "object")
                    .put("properties", new JSONObject().put("operationId", operationId))
                    .put("required", new org.json.JSONArray().put("operationId"))
                    .put("additionalProperties", false);
        }

        @Override
        public Object execute(JSONObject arguments) throws Exception {
            String operationId = StringUtils.trimToEmpty(arguments.optString("operationId", ""));
            try {
                return invoker.getAiHelpOperationHelp(operationId);
            }
            catch (Exception ex) {
                UniLog.log(ex);
                throw ex;
            }
        }
    }

    private static final class Turn {
        private final String question;
        private final String answer;

        private Turn(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }
}
