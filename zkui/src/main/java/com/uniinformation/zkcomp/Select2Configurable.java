package com.uniinformation.zkcomp;

/**
 * Server-side configuration contract for a component enhanced by Select2.
 *
 * <p>{@code ZkUtil.setupSelect2(...)} also supports legacy listboxes which do
 * not implement this interface by reading their existing custom attributes.</p>
 */
public interface Select2Configurable {
	String ATTRIBUTE_ENABLED = "select2-enable";
	String ATTRIBUTE_MULTIPLE = "select2-multiple";
	String ATTRIBUTE_TAGS = "select2-tags";
	String ATTRIBUTE_PLACEHOLDER = "placeholder";
	String ENABLED_VALUE = "Y";
	String DISABLED_VALUE = "N";

	boolean isSelect2Enabled();

	boolean isSelect2Multiple();

	boolean isSelect2Tags();

	String getSelect2Placeholder();
}
