package com.uniinformation.jx;

public class JxImageList extends JxField
{
	JxGadgetProvider provider = null;	
	public JxImageList(String p_fieldname)
	{
		super(p_fieldname);
		if(provider == null)
			provider = JxGadgetProvider.getProvider();
		if(provider != null) {						
			JxSkinElement node = provider.jxImageList();
			this.bind(node);
		}
	}

}
