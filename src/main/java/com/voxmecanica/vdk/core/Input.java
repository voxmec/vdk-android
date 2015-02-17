package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.DisplayablePart;
import com.voxmecanica.vdk.api.InputPart;

import java.util.ArrayList;
import java.util.List;

public class Input extends AbstractDialogPart implements InputPart, DisplayablePart {
	private String prompt;
	private String paramName;
	private List<String> values;
	private boolean displayed = true;
	
	public Input(String prompt, String param){
		setMetaPart(MetaPart.INPUT);
		this.prompt = prompt;
		this.paramName = param;
	}
	
	public Input(String prompt, String param, boolean isDisplayed){
		this(prompt, param);
		displayed = isDisplayed;
	}
	
	@Override
	public String getPrompt() {
		return prompt;
	}

	@Override
	public String getParamName() {
		return paramName;
	}
	
	@Override
	public List<String> getValues() {
		return (values != null) ? values : (values = new ArrayList<String>());
	}
	
	public void setValues(List<String> values){
		this.values = values;
	}
	
	public void addValue(String value){
		getValues().add(value);
	}

	@Override
	public String getTitle() {
		return getPrompt();
	}

	@Override
	public String getContent() {
		return (getValues() != null && getValues().size() > 0) ? getValues().get(0) : new String("");
	}

	@Override
	public boolean isDisplayed() {
		return displayed;
	}
}
