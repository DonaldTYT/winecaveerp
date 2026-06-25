package com.kyoko.common;

import java.io.IOException;

public interface StreamAcceptInterface {
	public boolean onStreamAccepted(ByteStream p_bs) throws IOException;
}
