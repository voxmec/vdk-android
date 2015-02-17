package com.voxmecanica.vdk.api;

import java.net.URI;

public interface PlayablePart extends OutputPart, DisplayablePart{
	public URI getResourceUri();
}
