package cn.dlb.bim.ifc.tree;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.PackageMetaData;

public class PropertyGenerator {
	
	@SuppressWarnings("rawtypes")
	public PropertySet getProperty(PackageMetaData packageMetaData, IdEObject product) {
		
		EClass productClass = packageMetaData.getEClass("IfcProduct");
		if (!productClass.isSuperTypeOf(product.eClass())) {
			return null;
		}
		
		PropertySet result = null;
		PropertySetCollection collection = new PropertySetCollection();
		EReference isDefinedByReference = packageMetaData.getEReference(product.eClass().getName(), "IsDefinedBy");
		if (isDefinedByReference != null) {
			List isDefinedByList = (List) product.eGet(isDefinedByReference);
			for (Object isDefinedBy : isDefinedByList) {
				if (isDefinedBy instanceof IdEObject) {
					IdEObject ifcRelDefinesByProperties = (IdEObject) isDefinedBy;
					if (packageMetaData.getEClass("IfcRelDefinesByProperties").isSuperTypeOf(ifcRelDefinesByProperties.eClass())) {
						EReference relatingPropertyDefinitionEReference = packageMetaData.getEReference(ifcRelDefinesByProperties.eClass().getName(), "RelatingPropertyDefinition");
						Object relatingPropertyDefinition = ifcRelDefinesByProperties.eGet(relatingPropertyDefinitionEReference);
						if (relatingPropertyDefinition instanceof IdEObject) {
							IdEObject ifcPropertySet = (IdEObject) relatingPropertyDefinition;
							result = generatePropertySet(packageMetaData, ifcPropertySet, collection);
						}
					}
				}
				
			}
		}
		return result;
	}
	
	@SuppressWarnings({ "rawtypes" })
	public PropertySet generatePropertySet(PackageMetaData packageMetaData, IdEObject ifcPropertySet, PropertySetCollection collection) {
		
		EClass ifcPropertySetClass = packageMetaData.getEClass("IfcPropertySet");
		if (!ifcPropertySetClass.isSuperTypeOf(ifcPropertySet.eClass())) {
			return null;
		}
		
		Long setOid = ifcPropertySet.getOid();
		String setName = (String) ifcPropertySet.eGet(ifcPropertySetClass.getEStructuralFeature("Name"));
		
		PropertySet propertySet = new PropertySet();
		propertySet.setOid(setOid);
		propertySet.setName(setName);
		
		List hasProperties = (List) ifcPropertySet.eGet(ifcPropertySetClass.getEStructuralFeature("HasProperties"));
		for (Object hasProperty : hasProperties) {
			if (hasProperty instanceof IdEObject) {
				IdEObject ifcPropertySingleValue = (IdEObject) hasProperty;
				generateProperty(packageMetaData, ifcPropertySingleValue, propertySet);
			}
		}
		
		return propertySet;
		
	}
	
	public void generateProperty(PackageMetaData packageMetaData, IdEObject ifcPropertySingleValue, PropertySet propertySet) {
		EClass ifcPropertySingleValueClass = packageMetaData.getEClass("IfcPropertySingleValue");
		if (!ifcPropertySingleValueClass.isSuperTypeOf(ifcPropertySingleValue.eClass())) {
			return;
		}
//		Long oid = ifcPropertySingleValue.getOid();
		String name = (String) ifcPropertySingleValue.eGet(ifcPropertySingleValueClass.getEStructuralFeature("Name"));
		Object nominalValue = ifcPropertySingleValue.eGet(ifcPropertySingleValueClass.getEStructuralFeature("NominalValue"));
		if (nominalValue != null) {
			IdEObject ifcValue = (IdEObject) nominalValue;
			if (ifcValue.eClass().getEAnnotation("wrapped") != null) {
				EStructuralFeature wrappedFeature = ifcValue.eClass().getEStructuralFeature("wrappedValue");
				Object value = ifcValue.eGet(wrappedFeature);
				Propertry property = new Propertry();
				property.setName(name);
				property.setValue(value);
				propertySet.getPropertiySet().add(property);
			}
			
		}
	}
}
