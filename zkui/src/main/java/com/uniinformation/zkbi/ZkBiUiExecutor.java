package com.uniinformation.zkbi;

import java.util.concurrent.Executor;

import org.zkoss.zk.ui.Component;

import com.uniinformation.utils.ZkUtil;

public class ZkBiUiExecutor implements Executor {
	private Runnable command;

    public ZkBiUiExecutor(Component comp) {
    	this(comp, null, 1000, null);
    }
    
    public ZkBiUiExecutor(Component comp, Runnable progressCmd) {
    	this(comp, null, 1000, progressCmd);
    }

    public ZkBiUiExecutor(Component comp, String busyMsg, Runnable progressCmd) {
    	this(comp, busyMsg, 1000, progressCmd);
    }

    public ZkBiUiExecutor(Component comp, String busyMsg, int delay, Runnable progressCmd) {
    	ZkUtil.timerEvent(null, comp, busyMsg, delay, true, true, () -> {
    		if (command != null) {
    			command.run();
    			return true;
    		}
    		if (progressCmd != null)
    			progressCmd.run();
    		return false;
		}, null, null);
    }
    
    @Override
    public void execute(Runnable command) {
    	this.command = command;
    }
}
