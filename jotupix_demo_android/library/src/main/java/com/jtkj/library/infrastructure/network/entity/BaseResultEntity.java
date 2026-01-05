package com.jtkj.library.infrastructure.network.entity;

import com.google.gson.Gson;

public class BaseResultEntity<T> {
	/**todo test for multiple request in one model instance*/
	public int requestCode;
	public Object extraParam;//需要返回的请求参数
	public String requestPage;//发送请求的页面

	//private String status;
	private String msg;
	private int code;
	private T data;

	/*public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}*/

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String toString() {
		Gson gson = new Gson();
		String str = gson.toJson(this);
		return str;
	}
}
