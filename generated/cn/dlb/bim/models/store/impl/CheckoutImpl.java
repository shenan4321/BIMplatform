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
package cn.dlb.bim.models.store.impl;

import java.util.Date;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;

import cn.dlb.bim.emf.IdEObjectImpl;
import cn.dlb.bim.models.log.CheckoutRelated;
import cn.dlb.bim.models.store.Checkout;
import cn.dlb.bim.models.store.Project;
import cn.dlb.bim.models.store.Revision;
import cn.dlb.bim.models.store.StorePackage;
import cn.dlb.bim.models.store.User;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Checkout</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link cn.dlb.bim.models.store.impl.CheckoutImpl#getUser <em>User</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.CheckoutImpl#getRevision <em>Revision</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.CheckoutImpl#getProject <em>Project</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.CheckoutImpl#getDate <em>Date</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.CheckoutImpl#getCheckin <em>Checkin</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.CheckoutImpl#getActive <em>Active</em>}</li>
 *   <li>{@link cn.dlb.bim.models.store.impl.CheckoutImpl#getLogs <em>Logs</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CheckoutImpl extends IdEObjectImpl implements Checkout {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CheckoutImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return StorePackage.Literals.CHECKOUT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected int eStaticFeatureCount() {
		return 0;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public User getUser() {
		return (User) eGet(StorePackage.Literals.CHECKOUT__USER, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setUser(User newUser) {
		eSet(StorePackage.Literals.CHECKOUT__USER, newUser);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Revision getRevision() {
		return (Revision) eGet(StorePackage.Literals.CHECKOUT__REVISION, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRevision(Revision newRevision) {
		eSet(StorePackage.Literals.CHECKOUT__REVISION, newRevision);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Project getProject() {
		return (Project) eGet(StorePackage.Literals.CHECKOUT__PROJECT, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setProject(Project newProject) {
		eSet(StorePackage.Literals.CHECKOUT__PROJECT, newProject);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Date getDate() {
		return (Date) eGet(StorePackage.Literals.CHECKOUT__DATE, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDate(Date newDate) {
		eSet(StorePackage.Literals.CHECKOUT__DATE, newDate);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Revision getCheckin() {
		return (Revision) eGet(StorePackage.Literals.CHECKOUT__CHECKIN, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCheckin(Revision newCheckin) {
		eSet(StorePackage.Literals.CHECKOUT__CHECKIN, newCheckin);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Boolean getActive() {
		return (Boolean) eGet(StorePackage.Literals.CHECKOUT__ACTIVE, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setActive(Boolean newActive) {
		eSet(StorePackage.Literals.CHECKOUT__ACTIVE, newActive);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<CheckoutRelated> getLogs() {
		return (EList<CheckoutRelated>) eGet(StorePackage.Literals.CHECKOUT__LOGS, true);
	}

} //CheckoutImpl
