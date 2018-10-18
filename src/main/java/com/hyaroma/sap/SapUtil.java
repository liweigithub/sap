package com.hyaroma.sap;

import com.alibaba.fastjson.JSON;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author wstv
 * 远程通过rfc协议调用 sap
 */
public class SapUtil {
	private static final Logger logger = LoggerFactory.getLogger(SapUtil.class);
	/**
	 * sapPoolName 既是和sap通讯的标识也是配置文件的名称
	 */
	private String sapPoolName;
	/**
	 * 获取rfc超时时限后每隔N秒重新发送获取rfc的请求 单位/秒
	 */
	private int getRfcWaitSeconds;
	/**
	 * 获取rfc超时时限 单位/分 超过 N分之后就不再获取rfc（只针对在发送数据时获取不到rfc的情况）
	 */
	private int getRfcMinute;
	/**
	 *sap给到的固定值，rfc请求名称
	 */
	private String rfcName;

	public SapUtil() {

	}
	public SapUtil(String sapPoolName) {
		this.sapPoolName = sapPoolName;
	}
	public void init(){
		try {
			logger.info("开始初始化 sapjco 配置..........【"+sapPoolName+"】");
			getRfcMinute = Integer.parseInt(PropertyPlaceholder.getProperty("sap.get_rfc_minute").toString());
			getRfcWaitSeconds = Integer.parseInt(PropertyPlaceholder.getProperty("sap.get_rfc_wait_seconds").toString())*1000;
			//测试rfc连通性
			Object rfcNameObj = PropertyPlaceholder.getProperty("sap.rfcName");
			if (rfcNameObj == null){
				throw  new WServiceException(SapUtil.class.getName()+" init exception , by conf [sap.rfcName] value is required，You can specify this through the configuration file ！");
			}
			rfcName  = rfcNameObj.toString();
			if ("".equals(sapPoolName)){
				//如果报该错，表示配置文件没有配置 和 sap 链接的标识 这个标识 可以通过配置文件配置 然后spring注入给 SapUtil 这样
				//sapPoolName 为什么会这样定义，考虑到可能需要对多个sap 服务器的情况，我们就可以通过sapPoolName 来指定 需要用到的pool标识以及通过该名字去加载对应的xxx.properties 配置文件
				//（sapPoolName 既是和sap通讯的标识也是配置文件的名称） 示例配置：
				/*
					<bean id="aqSapUtil" class="org.fuwushe.common.sapjco3.SapUtil" init-method="init">
						<property name="sapPoolName" value="${sap.server_pool_name_aq}" />
					</bean>
				*/
				throw  new WServiceException(SapUtil.class.getName()+" init exception , by prop [sapPoolName] value is required，You can specify this through the configuration file ！");
			}
			SapJcoStore.initStore(sapPoolName);
			try {
				SapHelper.getRfcFunction(rfcName, sapPoolName);
			} catch (Exception e) {
				logger.info("获取 rfc 传输对象 失败 ..........,请检查相关主机IP配置是否正确！");
				e.printStackTrace();
			}
		}catch (Exception e){
			e.printStackTrace();
			logger.info("初始化 sapjco 配置【"+sapPoolName+"】失败.........."+e.getMessage());
		}
	}
	/**
	 * 远程调用方法 wstv
	 * @param rfcName 调用rfc 模块名称
	 * @param methodCode rfc 函数code
	 * @param data 构造业务数据
	 * @return
	 */
	public  String createSend(String rfcName,String methodCode,String data) throws  WServiceException{
		//获取传输对象 JCoFunction
		JCoFunction func= null;
		try {
			func = SapHelper.getRfcFunction(rfcName, sapPoolName);
		} catch (Exception e) {
			logger.info("获取 rfc 传输对象 失败 ,尝试间断获取中..........");
			int count = 1;
			//递归调用，在规定的时间n分钟内，每n秒 重新执行一次获取 rfc
			func = reGetRfc( rfcName ,System.currentTimeMillis()+getRfcMinute*60*1000,count);
			e.printStackTrace();
		}
		if (func != null){
			Map<String,Object> rfcData = new HashMap<String,Object>();
			rfcData.put("IN_FTYP",methodCode);
			rfcData.put("IN_JSON",data);
			//向JCoFunction 填充数据
			func= fillData(func,rfcData);
			SapProxy proxy =new SapProxy();
			SapExecutor exec=new SapExecutor(proxy);
			//执行交互
			JCoFunction result = null;
			try {
				logger.info("发送 rfc 数据..........");
				//返回结果处理
				result = (JCoFunction)exec.processSap(sapPoolName, func);
				JCoParameterList exportParameterList = result.getExportParameterList();
				if (exportParameterList != null){
					return   exportParameterList.getString("OUT_JSON");
				}else{
					logger.info("返回 rfc 数据为空");
				}
			} catch (Exception e) {
				logger.info("发送 rfc 数据失败..........,请检查数据是否规范"+e.getMessage());
				e.printStackTrace();
			}
		}
		throw  new WServiceException("获取 rfc 传输对象 失败");
	}

	/**
	 * 填充数据,根据上面表格
	 * 在SAP中间件其实也就是解析在填充数据这里加工很多配置,实现同理
	 * @param jcoFunc
	 * @param importData 构造业务数据
	 * @return
	 */
	public static JCoFunction fillData(JCoFunction jcoFunc,Map<String, Object> importData){
		JCoParameterList parameterList = jcoFunc.getImportParameterList();
		//填充传入值
		for(String key:importData.keySet()){
			Object value=importData.get(key);
			parameterList.setValue(key, value);
		}
	//		上面构造数据只有传入的参数数据,下面是传入table的数据
			/*设置值

			JCoTable jcoTable= jcoFunc.getTableParameterList().getTable("");

			for(){

	//		增加一行数据，同时游标也只想了当前行
			jcoTable.appendRow();
	//		设置当前行的某个列的值
			jcoTable.setValue("", "");
			jcoTable.setValue("", "");
			}
		*/
		return jcoFunc;
	}
	
	/**
	 *
	 * @param paramMap
	 * @return
	 */
	public static String buildParam(Map<String,Object>  ... paramMap){
		List<Map<String,Object>> params = new ArrayList<Map<String,Object>>();
		for(Map<String,Object> map:paramMap){
			params.add(map);
		}
		return JSON.toJSONString(params);
	}
	//
	public static String buildParam(List<Map<String,Object>>  paramMap){
		List<Map<String,Object>> params = new ArrayList<Map<String,Object>>();
		for(Map<String,Object> map: paramMap){
			params.add(map);
		}
		return JSON.toJSONString(params);
	}

	public JCoFunction reGetRfc(String rfcName,long endTimeStamp,int count ){
		try {
			logger.info("尝试第："+count+"次获取");
			Date compDate = new Date(System.currentTimeMillis());
			Date endDate = new Date(endTimeStamp);
			System.out.println( compDate.compareTo(endDate));
			if ( compDate.compareTo(endDate) ==1){
				logger.info("获取 rfc 超时，请检查配置或者和SAP 服务商沟通解决。。。。");
				return null;
			}
			 return SapHelper.getRfcFunction(rfcName, sapPoolName);
		} catch (Exception e) {
			//两秒再执行一次获取
			try {
				Thread.sleep(getRfcWaitSeconds);
				count++;
				return reGetRfc(rfcName,endTimeStamp,count);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return null;
	}

	public String getSapPoolName() {
		return sapPoolName;
	}

	public void setSapPoolName(String sapPoolName) {
		this.sapPoolName = sapPoolName;
	}
}
