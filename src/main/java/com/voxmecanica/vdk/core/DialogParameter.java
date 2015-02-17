package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.ParameterPart;

public class DialogParameter implements ParameterPart {
	private String paramName;
	private String paramValue;
	
	public DialogParameter(String name, String value){
		paramName = name;
		paramValue = value;
	}
	
	@Override
	public String getName() {
		return paramName;
	}

	@Override
	public String getValue() {
		return paramValue;
	}

	@Override
	public String toString() {
		return "DialogParameter [Name=" + paramName + ", Value="
				+ paramValue + "]";
	}

	@Override
	public MetaPart getMetaPart() {
		return MetaPart.PARAM;
	}
}
