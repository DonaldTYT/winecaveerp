package com.uniinformation.utils;

import java.io.InputStream;
import java.io.Reader;

public class BiMedia implements org.zkoss.util.media.Media {
	
org.zkoss.util.media.Media media;

public BiMedia( org.zkoss.util.media.Media p_media) {
	media = p_media;
}

@Override
public byte[] getByteData() {
	return(media.getByteData());
}

@Override
public String getContentType() {
	return(media.getContentType());
}

@Override
public String getFormat() {
	return(media.getFormat());
}

@Override
public String getName() {
	return(media.getName());
}

@Override
public Reader getReaderData() {
	return(media.getReaderData());
}

@Override
public InputStream getStreamData() {
	return(media.getStreamData());
}

@Override
public String getStringData() {
	return(media.getStringData());
}

@Override
public boolean inMemory() {
	return(media.inMemory());
}

@Override
public boolean isBinary() {
	return(media.isBinary());
}

@Override
public boolean isContentDisposition() {
	return(media.isContentDisposition());
}
}
