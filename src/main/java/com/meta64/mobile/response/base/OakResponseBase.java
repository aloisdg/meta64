package com.meta64.mobile.response.base;

import com.meta64.mobile.util.ThreadLocals;

public class OakResponseBase {
	private boolean success;
	private String message;

	public OakResponseBase() {
		ThreadLocals.setResponse(this);
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
