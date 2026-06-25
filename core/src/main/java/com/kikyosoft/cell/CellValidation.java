package com.kikyosoft.cell;

import java.util.*;
import java.io.*;

public abstract class CellValidation implements Serializable
{
	static final long serialVersionUID = 4342103641033068407L;
	public abstract boolean validate(Cell p_cell,Object p_value);
	public abstract String getErrMsg();
}
