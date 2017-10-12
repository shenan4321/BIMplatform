package cn.dlb.bim.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GzipUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(GzipUtils.class);
	
	public static byte[] zipBytes(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {  
            return bytes;  
        } 
		try (
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				GZIPOutputStream gzpOs = new GZIPOutputStream(bos);
				) {
			gzpOs.write(bytes);
			gzpOs.close();
			bos.close();
			return bos.toByteArray();
		} catch (IOException e) {
			LOGGER.error("Can not zip bytes, please check.");
		}
		return bytes;
	}
	
	public static byte[] unzipBytes(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {  
            return bytes;  
        } 
		try (
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ByteArrayInputStream bis = new ByteArrayInputStream(bytes);  
				GZIPInputStream gunzip = new GZIPInputStream(bis);
				) {
	        byte[] buffer = new byte[256];  
	        int n;  
	        while ((n = gunzip.read(buffer)) >= 0) {  
	        	bos.write(buffer, 0, n);  
	        }  
	        bis.close();
	        gunzip.close();
	        bos.close();
			return bos.toByteArray();
		} catch (IOException e) {
			LOGGER.error("Can not unzip bytes, please check.");
		}
		return bytes;
	}
}
