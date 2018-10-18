package com.hyaroma.sap;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author wstv
 * SAP 连接管理的仓库,用来处理数据连接,连接池管理之类的类
 */
public class SapJcoStore {
	private static final Logger logger = LoggerFactory.getLogger(SapJcoStore.class);
	public static Hashtable<String, JCoDestination> destinations = new Hashtable<String, JCoDestination>();
	
	/**
	 * 获取连接池,利用JCoDestinationManager创建连接池放到表中
	 * 
	 * @param fdPoolName
	 * @return
	 * @throws Exception
	 */
	public static JCoDestination getDestination(String fdPoolName) throws Exception {
		JCoDestination destination = null;
		if (destinations.containsKey(fdPoolName)) {
			destination = destinations.get(fdPoolName);
		} else {
			destination = JCoDestinationManager.getDestination(fdPoolName);
			destinations.put(fdPoolName, destination);
		}
		return destination;
	}

	public Hashtable<String, JCoDestination> getDestinations() {
		return destinations;
	}

	/**
	 * 初始化
	 * @param poolName
	 * @throws Exception
	 */
	public static void initStore(String poolName) throws Exception {
		doInitialize(poolName);
	}

	/**
	 * 初始化SAP
	 * @param fdPoolName
	 * @return
	 * @throws Exception
	 */
	private static synchronized JCoDestination doInitialize(String fdPoolName)
			throws Exception {
		Properties sapConfig=new Properties();
//		Properties的配置项@see com.sap.conn.jco.ext.DestinationDataProvider
		InputStream  is=SapJcoStore.class.getResourceAsStream("/sap/"+ fdPoolName+".properties");
		sapConfig.load(is);
//		扩展参数,需要扩展参数的时候使用
		//sapConfig.setProperty((String)DestinationDataProvider.class.getField("JCO_SAPROUTER").get(DestinationDataProvider.class),"/H/219.141.250.71/H/");
		createDataFile(fdPoolName, "jcoDestination", sapConfig);
		JCoDestination  destination=getDestination(fdPoolName);
		if (destination != null){
			logger.info("初始化 sapjco 连接池【"+fdPoolName+"】成功");
		}
		return destination;
	}
	/**
	 * 创建连接文件(必须) 此处 主要是封装扩展参数用的。
	 * 底层代码获取配置文件获取的是（.jcoDestination） 结尾的文件，
	 * 如果不需要扩展参数，jco默认也会生成一个 .jcoDestination的文件
	 * @param name
	 * @param suffix
	 * @param properties
	 */
	private static void createDataFile(String name, String suffix, Properties properties) {
		File cfg = new File(name + "." + suffix);
		try {
			FileOutputStream fos = new FileOutputStream(cfg, false);
			properties.store(fos, "for connection");
			fos.close();
		} catch (Exception e) {
			throw new RuntimeException("Unable to create the destination file "+ cfg.getName(), e);
		}
	}
}
