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
import cn.dlb.bim.models.log.RevisionRelated;
import cn.dlb.bim.models.store.Revision;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Revision Related</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link cn.dlb.bim.models.log.impl.RevisionRelatedImpl#getRevision <em>Revision</em>}</li>
 * </ul>
 *
 * @generated
 */
public class RevisionRelatedImpl extends LogActionImpl implements RevisionRelated {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RevisionRelatedImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return LogPackage.Literals.REVISION_RELATED;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Revision getRevision() {
		return (Revision) eGet(LogPackage.Literals.REVISION_RELATED__REVISION, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRevision(Revision newRevision) {
		eSet(LogPackage.Literals.REVISION_RELATED__REVISION, newRevision);
	}

} //RevisionRelatedImpl
