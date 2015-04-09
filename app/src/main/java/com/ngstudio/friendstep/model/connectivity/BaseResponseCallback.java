package com.ngstudio.friendstep.model.connectivity;



public interface BaseResponseCallback<Response> {

	public void onSuccess(Response result);

	public void onError(Exception error);
}
