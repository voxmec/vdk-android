package com.voxmecanica.vdk.api;

import java.util.List;

public interface InputPart extends DialogPart {
	public String getPrompt();
	public String getParamName();
	public List<String> getValues();
}
