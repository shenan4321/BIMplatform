/**
 * Copyright (C) 2009-2014 BIMserver.org
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cn.dlb.bim.models.ifc4;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Ifc Rel Defines By Template</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link cn.dlb.bim.models.ifc4.IfcRelDefinesByTemplate#getRelatedPropertySets <em>Related Property Sets</em>}</li>
 *   <li>{@link cn.dlb.bim.models.ifc4.IfcRelDefinesByTemplate#getRelatingTemplate <em>Relating Template</em>}</li>
 * </ul>
 *
 * @see cn.dlb.bim.models.ifc4.Ifc4Package#getIfcRelDefinesByTemplate()
 * @model
 * @generated
 */
public interface IfcRelDefinesByTemplate extends IfcRelDefines {
	/**
	 * Returns the value of the '<em><b>Related Property Sets</b></em>' reference list.
	 * The list contents are of type {@link cn.dlb.bim.models.ifc4.IfcPropertySetDefinition}.
	 * It is bidirectional and its opposite is '{@link cn.dlb.bim.models.ifc4.IfcPropertySetDefinition#getIsDefinedBy <em>Is Defined By</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Related Property Sets</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Related Property Sets</em>' reference list.
	 * @see cn.dlb.bim.models.ifc4.Ifc4Package#getIfcRelDefinesByTemplate_RelatedPropertySets()
	 * @see cn.dlb.bim.models.ifc4.IfcPropertySetDefinition#getIsDefinedBy
	 * @model opposite="IsDefinedBy"
	 * @generated
	 */
	EList<IfcPropertySetDefinition> getRelatedPropertySets();

	/**
	 * Returns the value of the '<em><b>Relating Template</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link cn.dlb.bim.models.ifc4.IfcPropertySetTemplate#getDefines <em>Defines</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Relating Template</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Relating Template</em>' reference.
	 * @see #setRelatingTemplate(IfcPropertySetTemplate)
	 * @see cn.dlb.bim.models.ifc4.Ifc4Package#getIfcRelDefinesByTemplate_RelatingTemplate()
	 * @see cn.dlb.bim.models.ifc4.IfcPropertySetTemplate#getDefines
	 * @model opposite="Defines"
	 * @generated
	 */
	IfcPropertySetTemplate getRelatingTemplate();

	/**
	 * Sets the value of the '{@link cn.dlb.bim.models.ifc4.IfcRelDefinesByTemplate#getRelatingTemplate <em>Relating Template</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Relating Template</em>' reference.
	 * @see #getRelatingTemplate()
	 * @generated
	 */
	void setRelatingTemplate(IfcPropertySetTemplate value);

} // IfcRelDefinesByTemplate
