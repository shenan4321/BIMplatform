package cn.dlb.bim.web;

import java.util.Map;

public class ResultUtil {
	private Map<String, Object> result;

	public void setSuccess(Boolean success) {
		result.put("success", success);
	}
	public void setMsg(String msg) {
		result.put("msg", msg);
	}
	public Map<String, Object> getResult() {
		return result;
	}
	public void setData(Object data) {
		result.put("data", data);
	}
}
