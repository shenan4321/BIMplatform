package cn.dlb.bim.service.impl;

import org.eclipse.emf.ecore.EClass;
import org.springframework.data.util.CloseableIterator;

import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.service.CatalogService;

public class VirtualObjectCloseableIterator implements CloseableIterator<VirtualObject> {
	
	private CloseableIterator<VirtualObject> iterator;
	private CatalogService catalogService;
	
	public VirtualObjectCloseableIterator(CloseableIterator<VirtualObject> iterator, CatalogService catalogService) {
		this.iterator = iterator;
		this.catalogService = catalogService;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public VirtualObject next() {
		VirtualObject virtualObject = iterator.next();
		Short cid = virtualObject.getEClassId();
		EClass eClass = catalogService.getEClassForCid(cid);
		virtualObject.setEClass(eClass);
		return virtualObject;
	}

	@Override
	public void close() {
		iterator.close();
		iterator = null;
		catalogService = null;
	}

}
