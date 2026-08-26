package com.uniinformation.zkcomp;

import org.zkoss.zul.Listbox;

/**
 * ZK listbox configured for enhancement by the shared Select2 controller.
 *
 * <p>The component remains a normal {@link Listbox}; existing models,
 * selection APIs and {@code onSelect} listeners therefore remain compatible.</p>
 */
public class Select2Listbox extends Listbox implements Select2Configurable {
	private static final long serialVersionUID = 1L;

	public Select2Listbox() {
		setMold("select");
		setAttribute(ATTRIBUTE_ENABLED, ENABLED_VALUE);
		setAttribute(ATTRIBUTE_MULTIPLE, isMultiple() ? ENABLED_VALUE : DISABLED_VALUE);
	}

	@Override
	public void setMultiple(boolean multiple) {
		super.setMultiple(multiple);
		setAttribute(ATTRIBUTE_MULTIPLE, multiple ? ENABLED_VALUE : DISABLED_VALUE);
	}

	@Override
	public boolean isSelect2Enabled() {
		return ENABLED_VALUE.equals(getAttribute(ATTRIBUTE_ENABLED));
	}

	@Override
	public boolean isSelect2Multiple() {
		Object configured = getAttribute(ATTRIBUTE_MULTIPLE);
		return configured == null ? isMultiple() : ENABLED_VALUE.equals(configured);
	}

	@Override
	public boolean isSelect2Tags() {
		return ENABLED_VALUE.equals(getAttribute(ATTRIBUTE_TAGS));
	}

	@Override
	public String getSelect2Placeholder() {
		Object placeholder = getAttribute(ATTRIBUTE_PLACEHOLDER);
		return placeholder == null ? "" : String.valueOf(placeholder);
	}
}
