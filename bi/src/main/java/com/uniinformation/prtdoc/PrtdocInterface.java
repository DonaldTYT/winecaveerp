package com.uniinformation.prtdoc;

public interface PrtdocInterface {
	abstract public void print() throws Exception;
	abstract public PrtdocJson getPrintDocJson() throws Exception;
}
