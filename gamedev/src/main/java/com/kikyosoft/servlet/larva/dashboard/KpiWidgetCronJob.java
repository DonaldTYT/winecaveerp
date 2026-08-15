package com.kikyosoft.servlet.larva.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.cron.CronJob;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

/**
 * Background refresher for shell KPI widgets. The dashboard itself only reads
 * the agent-scoped cache maintained by {@link KpiWidgetConfigurator}.
 *
 * ShellKpiCronAgents is optional. When supplied on the server's default agent
 * it is a comma-separated list of shell agents whose KPI configuration is
 * refreshed by this one cron server.
 */
public class KpiWidgetCronJob extends CronJob {
	private SessionHelper cronSession;
	private final List<SessionHelper> shellSessions = new ArrayList<SessionHelper>();

	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception {
		cronSession = p_sh;
		String agents = StringUtils.trimToNull(BiConfig.getString(p_sh, "ShellKpiCronAgents"));
		if(agents == null) {
			shellSessions.add(p_sh);
			return;
		}
		for(String rawAgent : StringUtils.split(agents, ',')) {
			String agent = StringUtils.trimToNull(rawAgent);
			if(agent == null) continue;
			if(StringUtils.equals(agent, p_sh.getAgent())) {
				shellSessions.add(p_sh);
				continue;
			}
			SessionHelper shellSession = ZkSessionHelper.getSessionHelperDummy(agent,
					p_sh.getLoginId(), p_sh.getSvc());
			if(shellSession == null) {
				UniLog.log1("KPI cron cannot create shell session for agent %s", agent);
				continue;
			}
			shellSessions.add(shellSession);
		}
	}

	@Override
	public int runOnce() throws Exception {
		for(SessionHelper shellSession : shellSessions) {
			try {
				KpiWidgetConfigurator.refresh(shellSession);
			} catch(Exception ex) {
				UniLog.log1("KPI cron refresh failed for agent %s: %s",
						shellSession.getAgent(), ex.getMessage());
			}
		}
		return shellSessions.size();
	}

	@Override
	public int getPollTime() {
		return 20000;
	}

	@Override
	public void stop() {
		for(SessionHelper shellSession : shellSessions) {
			if(shellSession == cronSession) continue;
			try {
				shellSession.cleanSessionData();
			} catch(Exception ex) {
				UniLog.log(ex);
			}
		}
		shellSessions.clear();
	}
}
