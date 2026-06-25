package com.uniinformation.jx;

public interface JxFieldImageListInterface
{
	public int getIconByFileType(String p_filetype,String p_iconsize);
	public int imageListLoadImage(String p_filetype,String p_filename);
	public void imageListSetSize(int width,int height);
}
