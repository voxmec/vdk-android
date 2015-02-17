package com.voxmecanica.vdk.api;

import java.net.URI;
import java.util.List;

public interface Dialog {
	public List<DialogPart> getParts();
	public DialogRequest getSubmissionRequest();
	public URI getOriginUri();
}
