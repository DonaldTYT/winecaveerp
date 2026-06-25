package com.kikyosoft.cell;

import java.io.*;
import java.util.*;

public abstract class CellValueAction implements Serializable
{
	static final long serialVersionUID = -6640174414332896195L;
	public abstract void cellAction_onchange(Cell p_value) throws CellException;
	public abstract void cellAction_onfree() throws CellException;
}
