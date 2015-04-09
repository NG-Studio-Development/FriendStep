package com.ngstudio.friendstep.model;

public interface Callback<T, E> {
	public void onComplete(T result);

	public void onError(E error);

	public void anyway();
}
