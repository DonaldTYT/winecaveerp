package com.uniinformation.zkf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treecol;
import org.zkoss.zul.Treecols;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * JSON editor with multi-language support, rendered as a Tree.
 *
 * URL parameters:
 *   - jsonFile (required): primary JSON file full path
 *   - altFile  (optional, 0..n): alt JSON file full paths
 *   - ignore   (optional, 0..n): key names to ignore
 *
 * Tree columns:
 *   - col 0: key/path (e.g. hero/title, left/items/0/title) – Label, non-editable
 *   - col 1: primary value – Textbox
 *   - col 2..N: alt values – Textbox
 *
 * All leaf values are treated as strings.
 * Container nodes (JSONObject / JSONArray) get a Treeitem with:
 *   - attribute "jType" = "object" or "arrage"
 */
public class JsonEditorComposer extends SelectorComposer<Component> {

    @Wire
    private Div formHost;

    @Wire
    private Label msg;

    // optional, but fine if you wire them in zul
    @Wire
    private Button btnSave;

    @Wire
    private Button btExpand;

    @Wire
    private Button btCollapse;

    /** Primary JSON file path */
    private String primaryPath;

    /** Primary JSON object */
    private JSONObject primaryJson;

    /** altFile path → alt JSON */
    private final Map<String, JSONObject> altJsons = new LinkedHashMap<>();

    /** Ordered list of alt file paths, matching Tree columns */
    private final List<String> altPaths = new ArrayList<>();

    /** Names to ignore (key names, not paths) */
    private final Set<String> ignoreNames = new LinkedHashSet<>();

    /** Tree widget reference (for expand/collapse) */
    private Tree tree;

    /** path → primary Textbox */
    private final Map<String, Textbox> primaryTextFields = new LinkedHashMap<>();

    /** For each alt file: path → Textbox */
    private final List<Map<String, Textbox>> altTextFields = new ArrayList<>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        Execution exec = Executions.getCurrent();

        // 1) Primary jsonFile (required)
        primaryPath = exec.getParameter("jsonFile");
        if (primaryPath == null || primaryPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: jsonFile");
        }
        primaryJson = loadJsonFile(primaryPath);

        // 2) altFile(s) (0..n)
        altJsons.clear();
        altPaths.clear();
        String[] altFiles = exec.getParameterValues("altFile");
        if (altFiles != null) {
            for (String p : altFiles) {
                if (p == null) continue;
                String trimmed = p.trim();
                if (trimmed.isEmpty()) continue;
                JSONObject alt = loadJsonFile(trimmed);
                altJsons.put(trimmed, alt);
                altPaths.add(trimmed);
            }
        }

        // 3) ignore name(s) (0..n)
        ignoreNames.clear();
        String[] ignores = exec.getParameterValues("ignore");
        if (ignores != null) {
            for (String name : ignores) {
                if (name == null) continue;
                String trimmed = name.trim();
                if (trimmed.isEmpty()) continue;
                ignoreNames.add(trimmed);
            }
        }

        // Debug summary
        if (msg != null) {
            msg.setValue(buildInitSummary());
        }

        // 4) Render Tree view
        renderToTree();
    }

    // ───────────────────────── Rendering ─────────────────────────

    private void renderToTree() {
        if (formHost == null) {
            return;
        }

        formHost.getChildren().clear();
        primaryTextFields.clear();
        altTextFields.clear();
        for (int i = 0; i < altPaths.size(); i++) {
            altTextFields.add(new LinkedHashMap<String, Textbox>());
        }

        tree = new Tree();
        tree.setHflex("1");
        tree.setVflex("1");

        // Columns: Key + primary + one per alt
        Treecols cols = new Treecols();
        cols.setSizable(true);

        Treecol keyCol = new Treecol("Key");
        cols.appendChild(keyCol);

        String primaryName = new File(primaryPath).getName();
        Treecol primaryCol = new Treecol(primaryName);
        cols.appendChild(primaryCol);

        for (String altPath : altPaths) {
            String label = new File(altPath).getName();
            Treecol altCol = new Treecol(label);
            cols.appendChild(altCol);
        }

        tree.appendChild(cols);

        Treechildren rootChildren = new Treechildren();
        tree.appendChild(rootChildren);

        if (primaryJson != null) {
            renderJsonToTree(primaryJson, "", rootChildren);
        }

        formHost.appendChild(tree);
    }

    /**
     * Recursive walk of JSON structure, creating Treeitems.
     *
     * @param value          current JSON node (JSONObject / JSONArray / primitive)
     * @param path           full path, "/"-separated (hero/title, left/items/0/title)
     * @param parentChildren Treechildren to append to
     */
    private void renderJsonToTree(Object value, String path, Treechildren parentChildren) {
        if (value instanceof JSONObject) {
            JSONObject obj = (JSONObject) value;

            Treechildren childContainer = parentChildren;
            if (path != null && !path.isEmpty()) {
                Treeitem container = createContainerItem(path, "object");
                childContainer = new Treechildren();
                container.appendChild(childContainer);
                parentChildren.appendChild(container);
            }

            // collect & sort keys
            List<String> keys = new ArrayList<>();
            for (Iterator<String> it = obj.keys(); it.hasNext(); ) {
                keys.add(it.next());
            }
            Collections.sort(keys);

            for (String key : keys) {
                if (ignoreNames.contains(key)) {
                    continue;
                }
                Object child = obj.opt(key);
                String childPath = (path == null || path.isEmpty())
                        ? key
                        : (path + "/" + key);
                renderJsonToTree(child, childPath, childContainer);
            }

        } else if (value instanceof JSONArray) {
            JSONArray arr = (JSONArray) value;

            Treechildren childContainer = parentChildren;
            if (path != null && !path.isEmpty()) {
                Treeitem container = createContainerItem(path, "arrage");
                childContainer = new Treechildren();
                container.appendChild(childContainer);
                parentChildren.appendChild(container);
            }

            for (int i = 0; i < arr.length(); i++) {
                Object child = arr.opt(i);
                String idxStr = String.valueOf(i);
                String childPath = (path == null || path.isEmpty())
                        ? idxStr
                        : (path + "/" + idxStr);
                renderJsonToTree(child, childPath, childContainer);
            }

        } else {
            // Leaf (primitive) – treat as string
            String primaryVal;
            if (value == null || value == JSONObject.NULL) {
                primaryVal = "";
            } else {
                primaryVal = String.valueOf(value);
            }
            Treeitem leaf = createLeafItem(path, primaryVal);
            parentChildren.appendChild(leaf);
        }
    }

    /** Create a container node for JSONObject / JSONArray. */
    private Treeitem createContainerItem(String path, String jType) {
        Treeitem item = new Treeitem();
        item.setAttribute("jType", jType);  // "object" or "arrage"

        Treerow row = new Treerow();
        item.appendChild(row);

        // col 0: key/path
        Treecell keyCell = new Treecell(path == null ? "" : path);
        row.appendChild(keyCell);

        // col 1: empty
        Treecell primaryCell = new Treecell("");
        row.appendChild(primaryCell);

        // col 2..N: empty
        for (int i = 0; i < altPaths.size(); i++) {
            Treecell altCell = new Treecell("");
            row.appendChild(altCell);
        }

        return item;
    }

    /** Create a leaf row with Textboxes for primary + all alts and register them in maps. */
    private Treeitem createLeafItem(String path, String primaryVal) {
        Treeitem item = new Treeitem();
        Treerow row = new Treerow();
        item.appendChild(row);

        // col 0: path
        Treecell keyCell = new Treecell(path == null ? "" : path);
        row.appendChild(keyCell);

        // col 1: primary Textbox
        Textbox tbPrimary = new Textbox(primaryVal);
        tbPrimary.setHflex("1");
        Treecell primaryCell = new Treecell();
        primaryCell.appendChild(tbPrimary);
        row.appendChild(primaryCell);
        primaryTextFields.put(path, tbPrimary);

        // col 2..N: alt Textboxes
        for (int i = 0; i < altPaths.size(); i++) {
            JSONObject altRoot = altJsons.get(altPaths.get(i));
            String altVal = getValueFromAlt(altRoot, path);

            Textbox tbAlt = new Textbox(altVal);
            tbAlt.setHflex("1");

            Treecell altCell = new Treecell();
            altCell.appendChild(tbAlt);
            row.appendChild(altCell);

            altTextFields.get(i).put(path, tbAlt);
        }

        return item;
    }

    // ───────────────────────── Save handlers ─────────────────────────

    @Listen("onClick = #btnSave")
    public void onSave() {
        if (primaryJson == null) return;

        try {
            // 1) Update primary JSON from Textboxes
            for (Map.Entry<String, Textbox> e : primaryTextFields.entrySet()) {
                String path = e.getKey();
                String val = e.getValue().getValue();
                setValueAtPath(primaryJson, path, val);
            }

            // 2) Update alt JSONs from Textboxes
            for (int i = 0; i < altPaths.size(); i++) {
                String altPath = altPaths.get(i);
                JSONObject altRoot = altJsons.get(altPath);
                if (altRoot == null) continue;

                Map<String, Textbox> map = altTextFields.get(i);
                for (Map.Entry<String, Textbox> e : map.entrySet()) {
                    String path = e.getKey();
                    String val = e.getValue().getValue();
                    setValueAtPath(altRoot, path, val);
                }
            }

            // 3) Save to disk
            saveJsonToFile(primaryPath, primaryJson);
            for (String altPath : altPaths) {
                JSONObject altRoot = altJsons.get(altPath);
                if (altRoot != null) {
                    saveJsonToFile(altPath, altRoot);
                }
            }

            msg.setStyle("color:green;font-size:12px;");
            msg.setValue("JSON saved successfully.");
        } catch (Exception ex) {
            ex.printStackTrace();
            msg.setStyle("color:red;font-size:12px;");
            msg.setValue("Error saving JSON: " + ex.getMessage());
        }
    }

    // ───────────────────────── Expand / Collapse ─────────────────────────

    @Listen("onClick = #btExpand")
    public void onExpandAll() {
        if (tree == null) return;
        Treechildren children = tree.getTreechildren();
        if (children != null) {
            setOpenRecursive(children, true);
        }
    }

    @Listen("onClick = #btCollapse")
    public void onCollapseAll() {
        if (tree == null) return;
        Treechildren children = tree.getTreechildren();
        if (children != null) {
            setOpenRecursive(children, false);
        }
    }

    private void setOpenRecursive(Treechildren children, boolean open) {
        for (Object child : children.getChildren()) {
            if (child instanceof Treeitem) {
                Treeitem item = (Treeitem) child;
                item.setOpen(open);
                Treechildren sub = item.getTreechildren();
                if (sub != null) {
                    setOpenRecursive(sub, open);
                }
            }
        }
    }

    // ───────────────────────── JSON helpers ─────────────────────────

    private JSONObject loadJsonFile(String path) throws Exception {
        File f = new File(path);
        if (!f.exists()) {
            return new JSONObject();
        }
        String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        if (text.trim().isEmpty()) {
            return new JSONObject();
        }
        return new JSONObject(text);
    }

    private void saveJsonToFile(String path, JSONObject json) throws IOException {
        File f = new File(path);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        String content = json.toString(); // pretty print with 2 spaces
        Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Resolve a primitive value from an alt JSON by path ("/" separated).
     * Numeric segments are treated as array indices.
     */
    private String getValueFromAlt(JSONObject root, String path) {
        if (root == null || path == null || path.isEmpty()) {
            return "";
        }

        Object current = root;
        String[] parts = path.split("/");

        for (String part : parts) {
            if (current instanceof JSONObject) {
                JSONObject obj = (JSONObject) current;
                if (!obj.has(part)) {
                    return "";
                }
                current = obj.opt(part);
            } else if (current instanceof JSONArray) {
                JSONArray arr = (JSONArray) current;
                int idx;
                try {
                    idx = Integer.parseInt(part);
                } catch (NumberFormatException e) {
                    return "";
                }
                if (idx < 0 || idx >= arr.length()) {
                    return "";
                }
                current = arr.opt(idx);
            } else {
                return "";
            }
        }

        if (current == null || current == JSONObject.NULL) {
            return "";
        }
        if (current instanceof JSONObject || current instanceof JSONArray) {
            return "";
        }
        return String.valueOf(current);
    }

    /**
     * Set a string value at path in the given JSON root.
     * Creates intermediate objects/arrays when missing.
     *
     * Path syntax: "hero/title", "left/items/0/title"
     */
    private void setValueAtPath(JSONObject root, String path, String value) throws JSONException {
        if (root == null || path == null || path.isEmpty()) return;

        String[] parts = path.split("/");
        Object current = root;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            boolean last = (i == parts.length - 1);

            if (current instanceof JSONObject) {
                JSONObject obj = (JSONObject) current;
                if (last) {
                    obj.put(part, value);
                } else {
                    String nextPart = parts[i + 1];
                    boolean nextIsIndex = isInteger(nextPart);
                    Object child = obj.opt(part);

                    if (child == null || child == JSONObject.NULL ||
                            (nextIsIndex && !(child instanceof JSONArray)) ||
                            (!nextIsIndex && !(child instanceof JSONObject))) {
                        child = nextIsIndex ? new JSONArray() : new JSONObject();
                        obj.put(part, child);
                    }
                    current = child;
                }
            } else if (current instanceof JSONArray) {
                JSONArray arr = (JSONArray) current;
                int idx;
                try {
                    idx = Integer.parseInt(part);
                } catch (NumberFormatException e) {
                    // invalid index, abort
                    return;
                }

                ensureCapacity(arr, idx + 1);

                if (last) {
                    arr.put(idx, value);
                } else {
                    String nextPart = parts[i + 1];
                    boolean nextIsIndex = isInteger(nextPart);
                    Object child = arr.opt(idx);

                    if (child == null || child == JSONObject.NULL ||
                            (nextIsIndex && !(child instanceof JSONArray)) ||
                            (!nextIsIndex && !(child instanceof JSONObject))) {
                        child = nextIsIndex ? new JSONArray() : new JSONObject();
                        arr.put(idx, child);
                    }
                    current = child;
                }
            } else {
                // Unexpected structure; abort
                return;
            }
        }
    }

    private void ensureCapacity(JSONArray arr, int size) {
        while (arr.length() < size) {
            arr.put(JSONObject.NULL);
        }
    }

    private boolean isInteger(String s) {
        if (s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i == 0 && (c == '+' || c == '-')) continue;
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    // ───────────────────────── Misc ─────────────────────────

    private String buildInitSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Primary: ").append(primaryPath);

        if (!altPaths.isEmpty()) {
            sb.append(" | Alt files: ").append(altPaths);
        }

        if (!ignoreNames.isEmpty()) {
            sb.append(" | Ignore: ").append(ignoreNames);
        }

        return sb.toString();
    }

    public String getPrimaryPath() {
        return primaryPath;
    }

    public JSONObject getPrimaryJson() {
        return primaryJson;
    }

    public Map<String, JSONObject> getAltJsons() {
        return Collections.unmodifiableMap(altJsons);
    }

    public Set<String> getIgnoreNames() {
        return Collections.unmodifiableSet(ignoreNames);
    }
}
