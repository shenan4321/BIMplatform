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
package cn.dlb.bim.models.log.impl;

import org.eclipse.emf.ecore.EClass;

import cn.dlb.bim.models.log.LogPackage;
import cn.dlb.bim.models.log.NewProjectAdded;
import cn.dlb.bim.models.store.Project;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>New Project Added</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link cn.dlb.bim.models.log.impl.NewProjectAddedImpl#getParentProject <em>Parent Project</em>}</li>
 * </ul>
 *
 * @generated
 */
public class NewProjectAddedImpl extends ProjectRelatedImpl implements NewProjectAdded {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected NewProjectAddedImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LogPackage.Literals.NEW_PROJECT_ADDED;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Project getParentProject() {
		return (Project) eGet(LogPackage.Literals.NEW_PROJECT_ADDED__PARENT_PROJECT, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParentProject(Project newParentProject) {
		eSet(LogPackage.Literals.NEW_PROJECT_ADDED__PARENT_PROJECT, newParentProject);
	}

} //NewProjectAddedImpl
