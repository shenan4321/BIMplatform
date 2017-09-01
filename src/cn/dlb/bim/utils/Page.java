package cn.dlb.bim.utils;

import java.util.List;

public class Page<T> {
	
	private long total;
	private int pageNumber;
	private int pageSize;
	private List<T> rows;

	public void setTotal(long count) {
		total = count;
	}
	
	public long getTotal() {
		return total;
	}
	
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	
	public int getPageNumber() {
		return pageNumber;
	}
	
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setRows(List<T> rows) {
		this.rows = rows;
	}

	public List<T> getRows() {
		return rows;
	}

}
