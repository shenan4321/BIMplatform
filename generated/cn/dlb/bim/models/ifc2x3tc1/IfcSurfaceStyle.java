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
package cn.dlb.bim.models.ifc2x3tc1;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Ifc Surface Style</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link cn.dlb.bim.models.ifc2x3tc1.IfcSurfaceStyle#getSide <em>Side</em>}</li>
 *   <li>{@link cn.dlb.bim.models.ifc2x3tc1.IfcSurfaceStyle#getStyles <em>Styles</em>}</li>
 * </ul>
 *
 * @see cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcSurfaceStyle()
 * @model
 * @generated
 */
public interface IfcSurfaceStyle extends IfcPresentationStyle, IfcPresentationStyleSelect {
	/**
	 * Returns the value of the '<em><b>Side</b></em>' attribute.
	 * The literals are from the enumeration {@link cn.dlb.bim.models.ifc2x3tc1.IfcSurfaceSide}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Side</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Side</em>' attribute.
	 * @see cn.dlb.bim.models.ifc2x3tc1.IfcSurfaceSide
	 * @see #setSide(IfcSurfaceSide)
	 * @see cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcSurfaceStyle_Side()
	 * @model
	 * @generated
	 */
	IfcSurfaceSide getSide();

	/**
	 * Sets the value of the '{@link cn.dlb.bim.models.ifc2x3tc1.IfcSurfaceStyle#getSide <em>Side</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Side</em>' attribute.
	 * @see cn.dlb.bim.models.ifc2x3tc1.IfcSurfaceSide
	 * @see #getSide()
	 * @generated
	 */
	void setSide(IfcSurfaceSide value);

	/**
	 * Returns the value of the '<em><b>Styles</b></em>' reference list.
	 * The list contents are of type {@link cn.dlb.bim.models.ifc2x3tc1.IfcSurfaceStyleElementSelect}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Styles</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Styles</em>' reference list.
	 * @see cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package#getIfcSurfaceStyle_Styles()
	 * @model
	 * @generated
	 */
	EList<IfcSurfaceStyleElementSelect> getStyles();

} // IfcSurfaceStyle
