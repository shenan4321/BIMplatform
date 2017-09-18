package cn.dlb.bim.ifc.stream.deserializers;

import java.io.File;

/******************************************************************************
 * Copyright (C) 2009-2016  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

import java.io.InputStream;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;

import cn.dlb.bim.ifc.deserializers.DeserializeException;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.model.IfcHeader;
import cn.dlb.bim.ifc.shared.ByteProgressReporter;
import cn.dlb.bim.service.CatalogService;
import cn.dlb.bim.service.VirtualObjectService;

public interface StreamingDeserializer {
	void init(PackageMetaData packageMetaData, CatalogService catalogService, VirtualObjectService virtualObjectService);
	void setProgressReporter(ByteProgressReporter byteProgressReporter);
	long read(InputStream inputStream, String fileName, long fileSize) throws DeserializeException;
	public long read(File sourceFile) throws DeserializeException;
	IfcHeader getIfcHeader();
	Map<EClass, Integer> getSummaryMap();
}
