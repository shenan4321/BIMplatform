package cn.dlb.bim.ifc.engine;

import org.eclipse.emf.common.util.EList;

import cn.dlb.bim.ifc.engine.cells.Material;
import cn.dlb.bim.models.ifc2x3tc1.IfcColourOrFactor;
import cn.dlb.bim.models.ifc2x3tc1.IfcColourRgb;
import cn.dlb.bim.models.ifc2x3tc1.IfcMaterial;
import cn.dlb.bim.models.ifc2x3tc1.IfcMaterialDefinitionRepresentation;
import cn.dlb.bim.models.ifc2x3tc1.IfcMaterialLayer;
import cn.dlb.bim.models.ifc2x3tc1.IfcMaterialLayerSet;
import cn.dlb.bim.models.ifc2x3tc1.IfcMaterialLayerSetUsage;
import cn.dlb.bim.models.ifc2x3tc1.IfcMaterialList;
import cn.dlb.bim.models.ifc2x3tc1.IfcMaterialSelect;
import cn.dlb.bim.models.ifc2x3tc1.IfcPresentationStyleAssignment;
import cn.dlb.bim.models.ifc2x3tc1.IfcPresentationStyleSelect;
import cn.dlb.bim.models.ifc2x3tc1.IfcProduct;
import cn.dlb.bim.models.ifc2x3tc1.IfcProductRepresentation;
import cn.dlb.bim.models.ifc2x3tc1.IfcRelAssociates;
import cn.dlb.bim.models.ifc2x3tc1.IfcRelAssociatesMaterial;
import cn.dlb.bim.models.ifc2x3tc1.IfcRepresentation;
import cn.dlb.bim.models.ifc2x3tc1.IfcRepresentationItem;
import cn.dlb.bim.models.ifc2x3tc1.IfcStyledItem;
import cn.dlb.bim.models.ifc2x3tc1.IfcStyledRepresentation;
import cn.dlb.bim.models.ifc2x3tc1.IfcSurfaceStyle;
import cn.dlb.bim.models.ifc2x3tc1.IfcSurfaceStyleElementSelect;
import cn.dlb.bim.models.ifc2x3tc1.IfcSurfaceStyleRendering;

public class MaterialGetter {
	public void getMaterial(IfcProduct ifcProduct, Material material) {
		
		IfcProductRepresentation ifcProductRepresentation = ifcProduct.getRepresentation();
		
		
		EList<IfcRelAssociates> ifcRelAssociateses = ifcProduct.getHasAssociations();
		for (IfcRelAssociates ifcRelAssociates : ifcRelAssociateses) {
			if (ifcRelAssociates instanceof IfcRelAssociatesMaterial) {
				IfcRelAssociatesMaterial ifcRelAssociatesMaterial = (IfcRelAssociatesMaterial) ifcRelAssociates;
				IfcMaterialSelect ifcMaterialSelect = ifcRelAssociatesMaterial.getRelatingMaterial();
				if (ifcMaterialSelect instanceof IfcMaterial) {
					
				} else if (ifcMaterialSelect instanceof IfcMaterialList) {
					
				} else if (ifcMaterialSelect instanceof IfcMaterialLayerSetUsage) {
					IfcMaterialLayerSetUsage ifcMaterialLayerSetUsage = (IfcMaterialLayerSetUsage) ifcMaterialSelect;
					IfcMaterialLayerSet ifcMaterialLayerSet = ifcMaterialLayerSetUsage.getForLayerSet();
					EList<IfcMaterialLayer> ifcMaterialLayerAggr = ifcMaterialLayerSet.getMaterialLayers();
					int count = ifcMaterialLayerAggr.size();
					if (count != 0) {
						IfcMaterialLayer ifcMaterialLayer = ifcMaterialLayerAggr.get(0);
						IfcMaterial ifcMaterial = ifcMaterialLayer.getMaterial();
						EList<IfcMaterialDefinitionRepresentation> ifcMaterialDefinitionRepresentationAggr = ifcMaterial.getHasRepresentation();
						for (IfcMaterialDefinitionRepresentation ifcMaterialDefinitionRepresentation : ifcMaterialDefinitionRepresentationAggr) {
							getRgbIfcMaterialDefinitionRepresentation(ifcMaterialDefinitionRepresentation, material);
						}
						
					}
				} else if (ifcMaterialSelect instanceof IfcMaterialLayerSet) {
					
				} else if (ifcMaterialSelect instanceof IfcMaterialLayer) {
					
				}
			}
		}
	}
	
	private void getRgbProductDefinitionShape(IfcProductRepresentation ifcProductRepresentation, Material material) {
		EList<IfcRepresentation> representationsSet = ifcProductRepresentation.getRepresentations();
		for (IfcRepresentation ifcRepresentation : representationsSet) {
			String representationIdentifier = ifcRepresentation.getRepresentationIdentifier();
			String representationType = ifcRepresentation.getRepresentationType();
			
			if ((representationIdentifier.equals("Body") || representationIdentifier.equals("Mesh") || representationIdentifier.equals("Facetation")) && 
					!representationType.equals("BoundingBox")) {
				EList<IfcRepresentationItem> geometrySet = ifcRepresentation.getItems();
				for (IfcRepresentationItem geometry : geometrySet) {
					EList<IfcStyledItem> styledItemAggr = geometry.getStyledByItem();
					for (IfcStyledItem ifcStyledItem : styledItemAggr) {
						getRgbStyledItem(ifcStyledItem, material);
					}
				}
			}
		}
	}
	
	private void getRgbStyledItem(IfcStyledItem ifcStyledItem, Material material) {
		EList<IfcPresentationStyleAssignment> ifcPresentationStyleAssignmentAggr = ifcStyledItem.getStyles();
		for (IfcPresentationStyleAssignment ifcPresentationStyleAssignment : ifcPresentationStyleAssignmentAggr) {
			EList<IfcPresentationStyleSelect> ifcPresentationStyleSelectAggr = ifcPresentationStyleAssignment.getStyles();
			for (IfcPresentationStyleSelect ifcPresentationStyleSelect : ifcPresentationStyleSelectAggr) {
				if (ifcPresentationStyleSelect instanceof IfcSurfaceStyle) {
					IfcSurfaceStyle ifcSurfaceStyle = (IfcSurfaceStyle) ifcPresentationStyleSelect;
					EList<IfcSurfaceStyleElementSelect> ifcSurfaceStyleElementSelectAggr = ifcSurfaceStyle.getStyles();
					for (IfcSurfaceStyleElementSelect ifcSurfaceStyleElementSelect : ifcSurfaceStyleElementSelectAggr) {
						if (ifcSurfaceStyleElementSelect instanceof IfcSurfaceStyleRendering) {
							IfcSurfaceStyleRendering ifcSurfaceStyleRendering = (IfcSurfaceStyleRendering) ifcSurfaceStyleElementSelect;
							getRgbSurfaceStyle(ifcSurfaceStyleRendering, material);
						}
					}
				}
			}
		}
	}
	
	private void getRgbIfcMaterialDefinitionRepresentation(IfcMaterialDefinitionRepresentation ifcMaterialDefinitionRepresentation, Material material) {
		EList<IfcRepresentation> ifcRepresentationAggr = ifcMaterialDefinitionRepresentation.getRepresentations();
		for (IfcRepresentation ifcRepresentation : ifcRepresentationAggr) {
			if (ifcRepresentation instanceof IfcStyledRepresentation) {
				IfcStyledRepresentation ifcStyledRepresentation = (IfcStyledRepresentation) ifcRepresentation;
				EList<IfcRepresentationItem> ifcRepresentationItemAggr = ifcStyledRepresentation.getItems();
				for (IfcRepresentationItem ifcRepresentationItem : ifcRepresentationItemAggr) {
					if (ifcRepresentationItem instanceof IfcStyledItem) {
						IfcStyledItem ifcStyledItem = (IfcStyledItem) ifcRepresentationItem;
						getRgbStyledItem(ifcStyledItem, material);
					}
				}
			}
		}
	}
	
	private void getRgbSurfaceStyle(IfcSurfaceStyleRendering ifcSurfaceStyleRendering, Material material) {
		double transparency = ifcSurfaceStyleRendering.getTransparency();
		IfcColourRgb surfaceColour = ifcSurfaceStyleRendering.getSurfaceColour();
		double red = surfaceColour.getRed();
		double green = surfaceColour.getGreen();
		double blue = surfaceColour.getBlue();
		IfcColourOrFactor diffuseColour = ifcSurfaceStyleRendering.getDiffuseColour();
		IfcColourOrFactor specularColour = ifcSurfaceStyleRendering.getSpecularColour();
	}
}
