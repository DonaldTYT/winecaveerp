package com.uniinformation.zkf.vincero;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;

import com.kyoko.utils.ChartUtils;

import java.util.Arrays;

public class ZkCellActionTradeJournal2 extends SelectorComposer<Component> {
    @Wire
    private Div chartContainer;

    @Wire
    private Button plotButton;
    
    @Wire
    private Button clearButton;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        // Debugging: Check if components are wired correctly
        if (chartContainer == null) {
            System.out.println("chartContainer is NULL! Check @Wire annotation.");
        }
        if (plotButton == null) {
            System.out.println("plotButton is NULL! Ensure the button has id='plotButton' in ZUL.");
        }
        if (clearButton == null) {
            System.out.println("clearButton is NULL! Ensure the button has id='clearButton' in ZUL.");
        }

        // Add event listener to plot chart button
        plotButton.addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                System.out.println("PlotChart() called!"); // Debugging log
                if (chartContainer != null) {
                    ChartUtils.plotBarChart(chartContainer, 100, 0, Arrays.asList(50.0, 50.0, 30.0, 80.0, 60.0, 30.0 , 33.0, 45.0 , 20.2, 20.0, 88.2,50.2));
                }
            }
        });

        // Add event listener to clear chart button
        clearButton.addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                System.out.println("ClearChart() called!"); // Debugging log
                if (chartContainer != null) {
                    ChartUtils.clearChart(chartContainer);
                }
            }
        });
    }
}
