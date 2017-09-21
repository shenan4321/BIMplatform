package cn.dlb.bim.ifc.serializers;

import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import cn.dlb.bim.database.DatabaseException;

public interface StreamingReader {

	boolean write(OutputStream out) throws SerializerException, DatabaseException, InterruptedException, ExecutionException;
}