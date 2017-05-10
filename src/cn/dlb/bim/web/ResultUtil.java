package cn.dlb.bim.web;

import java.util.HashMap;
import java.util.Map;

public class ResultUtil {
	private Map<String, Object> result = new HashMap<String, Object>();

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
	public void setKeyValue(String key, Object value) {
		result.put(key, value);
	}
}
