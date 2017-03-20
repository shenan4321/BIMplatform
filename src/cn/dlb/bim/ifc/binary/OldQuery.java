package cn.dlb.bim.ifc.binary;

import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.QueryInterface;
import cn.dlb.bim.ifc.idm.ObjectIDM;

public class OldQuery implements QueryInterface {

	public static enum Deep {
		NO,
		YES
	}
	
	private final int pid;
	private final int rid;
	private final long roid;
	private final ObjectIDM objectIDM;
	private final Deep deep;
	private final int stopRid;
	private PackageMetaData packageMetaData;
	private Map<EClass, Long> oidCounters;
	
	private static final OldQuery DEFAULT = new OldQuery();
	
	public static final void setPackageMetaDataForDefaultQuery(PackageMetaData packageMetaData) {
		DEFAULT.packageMetaData = packageMetaData;
	}
	
	public static OldQuery getDefault() {
		return DEFAULT;
	}
	
	public void setOidCounters(Map<EClass, Long> oidCounters) {
		this.oidCounters = oidCounters;
	}
	
	public Map<EClass, Long> getOidCounters() {
		return oidCounters;
	}
	
	private OldQuery() {
		this.packageMetaData = null;
//		this.pid = Database.STORE_PROJECT_ID;TODO
		this.pid = 1;
		this.roid = -1;
		this.rid = Integer.MAX_VALUE;
		this.stopRid = Integer.MIN_VALUE;
		this.objectIDM = null;
		this.deep = Deep.NO;
	}
	
	public OldQuery(PackageMetaData packageMetaData, int pid, int rid, long roid) {
		this.packageMetaData = packageMetaData;
		this.pid = pid;
		this.rid = rid;
		this.roid = roid;
		this.stopRid = Integer.MIN_VALUE;
		this.objectIDM = null;
		this.deep = Deep.NO;
	}
	
	public OldQuery(PackageMetaData packageMetaData, boolean deep) {
		this.packageMetaData = packageMetaData;
//		this.pid = Database.STORE_PROJECT_ID;TODO
		this.pid = 1;
		this.rid = Integer.MAX_VALUE;
		this.roid = -1;
		this.stopRid = Integer.MIN_VALUE;
		this.objectIDM = null;
		this.deep = deep ? Deep.YES : Deep.NO;
	}

	public OldQuery(PackageMetaData packageMetaData, int pid, int rid, long roid, Deep deep) {
		this.packageMetaData = packageMetaData;
		this.objectIDM = null;
		this.pid = pid;
		this.rid = rid;
		this.roid = roid;
		this.stopRid = Integer.MIN_VALUE;
		this.deep = deep;
	}

	public OldQuery(PackageMetaData packageMetaData, int pid, int rid, long roid, ObjectIDM objectIDM, Deep deep) {
		this.packageMetaData = packageMetaData;
		this.pid = pid;
		this.rid = rid;
		this.roid = roid;
		this.stopRid = Integer.MIN_VALUE;
		this.objectIDM = objectIDM;
		this.deep = deep;
	}
	
	public OldQuery(PackageMetaData packageMetaData, int pid, int rid, long roid, ObjectIDM objectIDM, Deep deep, int stopRid) {
		this.packageMetaData = packageMetaData;
		this.pid = pid;
		this.rid = rid;
		this.roid = roid;
		this.stopRid = stopRid;
		this.objectIDM = objectIDM;
		this.deep = deep;
	}

//	public void updateOidCounters(ConcreteRevision subRevision, DatabaseSession databaseSession) {
//		if (subRevision.getOidCounters() != null) {
//			Map<EClass, Long> oidCounters = new HashMap<>();
//			ByteBuffer buffer = ByteBuffer.wrap(subRevision.getOidCounters());
//			for (int i=0; i<buffer.capacity() / 8; i++) {
//				buffer.order(ByteOrder.LITTLE_ENDIAN);
//				long oid = buffer.getLong();
//				buffer.order(ByteOrder.BIG_ENDIAN);
//				EClass eClass = databaseSession.getEClass((short)oid);
//				oidCounters.put(eClass, oid);
//			}
//			setOidCounters(oidCounters);
//		}
//	}
	
	public int getStopRid() {
		return stopRid;
	}

	public int getPid() {
		return pid;
	}
	
	public int getRid() {
		return rid;
	}

	public boolean isDeep() {
		return deep == Deep.YES;
	}
	
	public ObjectIDM getObjectIDM() {
		return objectIDM;
	}
	
	public boolean shouldIncludeClass(EClass eClass) {
		return objectIDM == null || objectIDM.shouldIncludeClass(eClass, eClass);
	}

	public boolean shouldFollowReference(EClass originalQueryClass, EClass eClass, EStructuralFeature feature) {
		return objectIDM == null || objectIDM.shouldFollowReference(originalQueryClass, eClass, feature);
	}
	
	@Override
	public PackageMetaData getPackageMetaData() {
		return packageMetaData;
	}

	@Override
	public long getRoid() {
		return roid;
	}
}