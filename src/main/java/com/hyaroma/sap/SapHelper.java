package com.hyaroma.sap;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wstv
 */
public class SapHelper {
	private static final Logger logger = LoggerFactory.getLogger(SapHelper.class);

	/**
	 * 根据RFC 函数名称,连接池获取函数
	 * 
	 * @param rfcName
	 * @param fdPoolName
	 * @return
	 * @throws Exception
	 */
	public static JCoFunction getRfcFunction(String rfcName,
			String fdPoolName) throws Exception {
		JCoDestination destination = SapJcoStore.getDestination(fdPoolName);
		JCoRepository repository = null;
		try {
			repository = destination.getRepository();
			logger.info("获取 sapjco 连接成功！");
		} catch (Exception e) {
			destination = JCoDestinationManager.getDestination(fdPoolName);
			SapJcoStore.destinations.remove(fdPoolName);
			SapJcoStore.destinations.put(fdPoolName, destination);
			repository = destination.getRepository();
		}
		JCoFunction  function=  destination.getRepository().getFunction(rfcName);// 获取RFC
		return function;
	}



}
