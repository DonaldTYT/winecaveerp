package com.uniinformation.zkf.propertymgmt;

import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

public class DynamicButtonComposer extends GenericForwardComposer<Window> {
    private Label lblStatus;

    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);

        // Create Label to Show Click Status
        lblStatus = new Label("Click inside the button...");
        lblStatus.setStyle("font-weight:bold;");
        comp.appendChild(lblStatus);

        // Create Two Buttons Dynamically
        Button button1 = createCustomButton("Button 1");
        Button button2 = createCustomButton("Button 2");

        // Add buttons to the window
        comp.appendChild(button1);
        comp.appendChild(new Separator());
        comp.appendChild(button2);

        // Inject JavaScript to track mouse drag and reset colors when exiting buttons
        Clients.evalJavaScript(
            "let lastHoveredButton = null;" +  // Store the last hovered button
            "let lastDownButton = null;" +  // Store the last hovered button
            "document.addEventListener('mousedown', function(event) {" +
            "   let target = event.target;" +
            "   if (target && target.classList.contains('custom-btn')) {" +
            "       console.log(\"mousedown catched\");" +
            "       lastDownButton = target;" +
            "       let zkTarget = zk.Widget.$(lastDownButton);" + // Convert to ZK component
            "       zAu.send(new zk.Event(zkTarget, 'onMyMouseDown', { message: \"Hello from JS!\" }));"+ // Send event to ser
            "       console.log(\"event sent\");" +
            "   }" +
            "});" +
            "document.addEventListener('mousemove', function(event) {" +
            "   let target = event.target;" +
            "   if (target && target.classList.contains('custom-btn')) {" +
            "       if (lastHoveredButton && lastHoveredButton !== target) {" +
            "           lastHoveredButton.style.backgroundColor = 'blue';" + // Reset previous button color
            "       }" +
            "       target.style.backgroundColor = 'lightblue';" + // Highlight hovered button
            "       lastHoveredButton = target;" +
            "   }" +
            "});" +
            "document.addEventListener('mouseup', function(event) {" +
            "   console.log(\"mouseup catched\");" +
            "   if (lastHoveredButton) {" +
            "       lastHoveredButton.style.backgroundColor = 'blue';" + // Reset final button color
            "       let zkTarget = zk.Widget.$(lastHoveredButton);" + // Convert to ZK component
            "       let target = event.target;" + // Convert to ZK component
            "       if (target && target != lastDownButton) {" +
            "           zkTarget.fire('onClick');" + // Fire onClick event
            "       }" +
            "   }" +
            "   lastHoveredButton = null;" + // Reset tracking
            "   lastDownButton = null;" + // Reset tracking
            "});" +
            "document.addEventListener('mouseleave', function(event) {" + // Detect mouse leaving
            "   if (lastHoveredButton) {" +
            "       lastHoveredButton.style.backgroundColor = 'blue';" + // Reset color
            "       lastHoveredButton = null;" + // Clear tracking
            "   }" +
            "}, true);" // `true` ensures it captures all elements
        );
    }
    private Button createCustomButton(String label) {
        Button button = new Button(label);
        button.setId(label.replace(" ", "_")); // Ensure unique ID (e.g., "Button_1")
        button.setSclass("custom-btn"); // Apply class for JavaScript to detect
        button.setStyle("background-color: blue; color: white; border: none; padding: 10px 20px; font-size: 16px; cursor: pointer;");

        // Handle the onClick event (updates the label)
        button.addEventListener("onClick", event -> { 
        	System.out.println(button.getLabel() + " Clicked");
            lblStatus.setValue(button.getLabel() + " Clicked on Mouse Release!");
        });
        button.addEventListener("onMyMouseDown", event -> { 
        	System.out.println(button.getLabel() + " MouseDown");
        });

        return button;
    }
}
