package com.voxmecanica.vdk.api;

public interface SpeakablePart extends OutputPart, DisplayablePart{
	public String getTextToSpeak();
	public float getSpeachRate();
	public float getSpeachPitch();
}
