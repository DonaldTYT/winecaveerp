package com.uniinformation.zkbi;

import org.zkoss.zk.ui.Component;

import com.uniinformation.webcore.SessionHelper;

/**
 * Convenience base for a read-only AI context contributed by one
 * {@link BiActionHandler}.
 *
 * <p>Implementations only need to provide their context JSON, operation
 * catalog and operation help. Session and parent-component access are
 * delegated to the owning composer.</p>
 */
public abstract class ZkBiActionAIHelperContext implements ZkBiAiAgentContext {
    private final ZkBiComposerBase composer;

    protected ZkBiActionAIHelperContext(ZkBiComposerBase composer) {
        if (composer == null)
            throw new IllegalArgumentException("composer is required");
        this.composer = composer;
    }

    protected final ZkBiComposerBase getComposer() {
        return composer;
    }

    @Override
    public final SessionHelper getAiHelpSessionHelper() {
        return composer.getAiHelpSessionHelper();
    }

    @Override
    public final Component getAiHelpParentComponent() {
        return composer.getAiHelpParentComponent();
    }
}
