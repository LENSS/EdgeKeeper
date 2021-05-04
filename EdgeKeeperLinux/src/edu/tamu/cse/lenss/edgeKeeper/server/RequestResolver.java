package edu.tamu.cse.lenss.edgeKeeper.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.tamu.cse.lenss.edgeKeeper.utils.EKRecord;
import edu.tamu.cse.lenss.edgeKeeper.utils.EKUtils;
import edu.tamu.cse.lenss.edgeKeeper.zk.ZKClientHandler;

public class RequestResolver{
	public static final Logger logger = Logger.getLogger(RequestResolver.class);

	GNSClientHandler gnsClientHandler;
	ZKClientHandler zkClientHandler;
    
    public RequestResolver(GNSClientHandler gnsClientHandler, 
    		ZKClientHandler zkClientHandler){
    	this.gnsClientHandler = gnsClientHandler;
    	this.zkClientHandler = zkClientHandler;
	}

	
	public boolean addService(String serviceName, String duty) {
		return updateFieldStrict(serviceName, duty);
//		boolean result = EKHandler.ekRecord.updateField(serviceName, duty);
//		logger.debug("Updated the service to own record ");
//		triggerUpdate();
//		return result;
	}
	
	
	//Added by Amran (Unfinished)
    //This method is written by Amran. ####################################################
    //     _                              
    //    / \   _ __ ___  _ __ __ _ _ __  
    //   / _ \ | '_ ` _ \| '__/ _` | '_ \ 
    //  / ___ \| | | | | | | | (_| | | | |
    // /_/   \_\_| |_| |_|_|  \__,_|_| |_|
	
	public boolean addServiceExt(String serviceName, String serviceID, String duty, String ip, String port) {
		return updateFieldStrictlyStrict(serviceName, serviceID, duty, ip, port);
//		boolean result = EKHandler.ekRecord.updateField(serviceName, duty);
//		logger.debug("Updated the service to own record ");
//		triggerUpdate();
//		return result;
	}
	
	//Added by Amran (Unfinished)
    //This method is written by Amran. ####################################################
    //     _                              
    //    / \   _ __ ___  _ __ __ _ _ __  
    //   / _ \ | '_ ` _ \| '__/ _` | '_ \ 
    //  / ___ \| | | | | | | | (_| | | | |
    // /_/   \_\_| |_| |_|_|  \__,_|_| |_|
	
	public boolean addServiceExt(String serviceName, String serviceID, String duty) {
		return updateFieldStrictlyStrict(serviceName, serviceID, duty);
//		boolean result = EKHandler.ekRecord.updateField(serviceName, duty);
//		logger.debug("Updated the service to own record ");
//		triggerUpdate();
//		return result;
	}

	public boolean removeService(String serviceName) {
		return updateFieldLazy(serviceName, "NULL");
//		boolean result = EKHandler.ekRecord.removeField(serviceName);
//		logger.debug("Removed service from own record");
//		triggerUpdate();
//		return removeFieldStrict(serviceName);
	}

	
	private boolean updateFieldStrict(String field, Object value) {
		//gnsClientHandler.update(EKHandler.ekRecord);
		//zkClientHandler.update(EKHandler.ekRecord);
		
		JSONObject record = EKHandler.ekRecord.fetchRecord();
		try {
			record.put(field, value);
		} catch (JSONException e) {
			logger.error("Could not add these key pair in the record: "+field+", "+value);
			return false;
		}
		if(zkClientHandler.update(record)) {
			EKHandler.ekRecord.updateField(field, value);
			gnsClientHandler.update();
			return true;
		}
		else {
			logger.debug("Could not update to Zookeeper. So, discarding the update.");
			return false;
		}
	}
	
	
	//Added by Amran (Almost Finished)
    //This method is written by Amran. ####################################################
    //     _                              
    //    / \   _ __ ___  _ __ __ _ _ __  
    //   / _ \ | '_ ` _ \| '__/ _` | '_ \ 
    //  / ___ \| | | | | | | | (_| | | | |
    // /_/   \_\_| |_| |_|_|  \__,_|_| |_|
	
	private boolean updateFieldStrictlyStrict(String fieldService, String serviceID, String fieldDuty, String fieldIP, String fieldPort) {
		//gnsClientHandler.update(EKHandler.ekRecord);
		//zkClientHandler.update(EKHandler.ekRecord);
		//serviceID = "ABCDXYZ";
		JSONObject obj = new JSONObject();
		try {
			obj.put(fieldService, fieldDuty);
			obj.put(RequestTranslator.fieldIP, fieldIP);
			obj.put(RequestTranslator.fieldPort, fieldPort);
		} catch (JSONException e1) {
			logger.error("Could not put objects with error: ",e1);
		}
	
		JSONObject record = EKHandler.ekRecord.fetchRecord();
		String allServices = null;
		
		try {
			if(record.isNull(RequestTranslator.allServicesField)){
				allServices = serviceID;
			} else {
				allServices = record.getString(RequestTranslator.allServicesField)+"/"+serviceID;
			}

			record.put(serviceID, obj);
			record.put(RequestTranslator.allServicesField, allServices);

			if(zkClientHandler.update(record)) {
				EKHandler.ekRecord.updateField(RequestTranslator.allServicesField, allServices);
				EKHandler.ekRecord.updateField(serviceID, obj);
				gnsClientHandler.update();
				return true;
			}
			else {
				logger.debug("Could not update to Zookeeper. So, discarding the update.");
				return false;
			} 
		} catch (JSONException e) {
			logger.error("Could not add these key pair in the record: " + serviceID + ", "+ obj);
			return false;
		}
	}
	
	
	
	//Added by Amran (Almost Finished)
    //This method is written by Amran. ####################################################
    //     _                              
    //    / \   _ __ ___  _ __ __ _ _ __  
    //   / _ \ | '_ ` _ \| '__/ _` | '_ \ 
    //  / ___ \| | | | | | | | (_| | | | |
    // /_/   \_\_| |_| |_|_|  \__,_|_| |_|
	
	private boolean updateFieldStrictlyStrict(String fieldService, String serviceID, String fieldDuty) {
		//gnsClientHandler.update(EKHandler.ekRecord);
		//zkClientHandler.update(EKHandler.ekRecord);
		//serviceID = "ABCDXYZ";
			
		JSONObject record = EKHandler.ekRecord.fetchRecord();
		String allServices = null;
		
		try {
			if(record.isNull(RequestTranslator.allServicesField)){
				allServices = serviceID;
			} else {
				allServices = record.getString(RequestTranslator.allServicesField)+"/"+serviceID;
			}

			record.put(serviceID, fieldService+"-"+fieldDuty);
			record.put(RequestTranslator.allServicesField, allServices);

			if(zkClientHandler.update(record)) {
				EKHandler.ekRecord.updateField(RequestTranslator.allServicesField, allServices);
				EKHandler.ekRecord.updateField(serviceID, fieldService+"-"+fieldDuty);
				//gnsClientHandler.update();
				return true;
			}
			else {
				logger.debug("Could not update to Zookeeper. So, discarding the update.");
				return false;
			} 
		} catch (JSONException e) {
			logger.error("Could not add these key pair in the record: " + serviceID + ":" + fieldService+"-"+fieldDuty);
			return false;
		}
	}
	
	
	
	
	
	private boolean updateFieldLazy(String field, Object value) {
		EKHandler.ekRecord.updateField(field, value);
		zkClientHandler.updateCurrentRecord(); 
		gnsClientHandler.update();
		return true;
	}

	
//	private boolean removeFieldStrict(String field) {
//		JSONObject record = EKHandler.ekRecord.fetchRecord();
//		record.remove(field);
//		if(zkClientHandler.update(record)) {
//			EKHandler.ekRecord.removeField(field);
//			gnsClientHandler.update();
//			return true;
//		}
//		else {
//			logger.debug("Could not update to Zookeeper. So, discarding the update.");
//			return false;
//		}
//	}


	public boolean updateIP() {
		Set<String> ownIPs = EKUtils.getAllAPs();
		return updateIP(ownIPs); 
	}
	
	public boolean updateIP(Set<String> ownIPs) {
		boolean result;
		try {
	        logger.log(Level.ALL," Trying to update own IP to the GNS server");
	        
	        JSONArray ownAddrJArray = new JSONArray();
	        for (String addr: ownIPs)
	            ownAddrJArray.put(addr);
	
	        /*	Now, update A record for DNS
	         * A record data type:
	         * A:	{
	         * 			record: [ip1,ip2...],
	         * 			ttl: 60
	         *		}
	         */
	        JSONObject recordObj = new JSONObject();
	        recordObj.put(EKRecord.RECORD_FIELD, ownAddrJArray);
	        recordObj.put(EKRecord.TTL_FIELD, EKRecord.default_ttl);
	        
	        //updateField(field, value)
	        
	        result = updateFieldLazy(EKRecord.A_RECORD_FIELD, recordObj);
	        result = updateFieldLazy(EKRecord.fieldIP,ownAddrJArray);
	
	        logger.debug("Updated IP fields to own record");
	        //triggerUpdate();
		} catch(NullPointerException | JSONException e) {
			logger.error("Problem in creating own name record", e);
			result = false;
		}
		return result;
	}

	public String getOwnGUID() {
		return gnsClientHandler.getOwnGUID();
	}

	public String getOwnAccountName() {
		return gnsClientHandler.getOwnAccountName();
	}

	public  JSONArray getPeerGUIDs(String service, String duty) {
		List<String> local = zkClientHandler.getPeerGUIDs(service, duty);
		logger.log(Level.ALL, "Local query returns: "+local);
		
		List<String> global = gnsClientHandler.getPeerGUIDs(service, duty);
			logger.log(Level.ALL, "Global query returnes: "+global);
		
		return jArrayMerge(local, global);
	}
	
	
	
	//Added by Amran (Unfinished)
    //This method is written by Amran. ####################################################
    //     _                              
    //    / \   _ __ ___  _ __ __ _ _ __  
    //   / _ \ | '_ ` _ \| '__/ _` | '_ \ 
    //  / ___ \| | | | | | | | (_| | | | |
    // /_/   \_\_| |_| |_|_|  \__,_|_| |_|
	
	public  JSONArray getPeerInfo(String service, String duty) {
		Map<String, List<String>> local = zkClientHandler.getPeerInfo(service, duty);
		logger.log(Level.ALL, "Local query returns get Peer info -----------------------------------------: "+local);
		
//		Map<String, List<String>> global = gnsClientHandler.getPeerInfo(service, duty);
//		logger.log(Level.ALL, "Global query returnes: "+global);
		
		//local.putAll(global);
		JSONObject obj=new JSONObject(local);
		JSONArray array = null;
		try {
			array = new JSONArray("["+obj.toString()+"]");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return array;
		
	}
	
	//Added by Amran (Unfinished)
    //This method is written by Amran. ####################################################
    //     _                              
    //    / \   _ __ ___  _ __ __ _ _ __  
    //   / _ \ | '_ ` _ \| '__/ _` | '_ \ 
    //  / ___ \| | | | | | | | (_| | | | |
    // /_/   \_\_| |_| |_|_|  \__,_|_| |_|
	
	public  JSONArray getPeerList(String service, String duty) {
		Map<String, List<String>> local = zkClientHandler.getPeerList(service, duty);
		logger.log(Level.ALL, "Local query returns get Peer info -----------------------------------------: "+local);
		
//		Map<String, List<String>> global = gnsClientHandler.getPeerInfo(service, duty);
//		logger.log(Level.ALL, "Global query returnes: "+global);
		
		//local.putAll(global);
		JSONObject obj=new JSONObject(local);
		JSONArray array = null;
		try {
			array = new JSONArray("["+obj.toString()+"]");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return array;
		
	}

	
	
	
	
	public JSONArray getIPsFromGuid(String guid) {
		List<String> local = zkClientHandler.getIPsFromGuid(guid);
		logger.log(Level.ALL, "Local query returns: "+local);
		
		List<String> global = gnsClientHandler.getIPsFromGuid(guid);
		logger.log(Level.ALL, "Global query returnes: "+global);
		
		return jArrayMerge(local, global);
	}

	public String getAccountNamebyGUID(String guid) {
		String local = zkClientHandler.getAccountNamebyGUID( guid);
		logger.log(Level.ALL, "Local query returns: "+local);
		
		String global = gnsClientHandler.getAccountNamebyGUID( guid);
			logger.log(Level.ALL, "Global query returnes: "+global);
		
		return compareAndAction(local,global);
	}
	
	public String getGUIDbyAccountName(String AccountName) {
		String local = zkClientHandler.getGUIDbyAccountName( AccountName);
		logger.log(Level.ALL, "Local query returns: "+local);
		
		String global = gnsClientHandler.getGUIDbyAccountName( AccountName);
			logger.log(Level.ALL, "Global query returnes: "+global);
		
		return compareAndAction(local,global);
	}

	public JSONArray getGUIDbyIP(String ip) {
		List<String> local = zkClientHandler.getGUIDbyIP(ip);
		logger.log(Level.ALL, "Local query returns: "+local);
		
		List<String> global = gnsClientHandler.getGUIDbyIP(ip);
			logger.log(Level.ALL, "Global query returnes: "+global);
		
		return jArrayMerge(local, global);
	}


	private String compareAndAction(String local, String global) {
		if(local == null && global==null)
			return "";
		if(local == null)
			return global;
		if (global==null)
			return local;
		
		if(!local.equals(global))
			logger.warn("local and global strings are different. local: "+local+" global:"+global);
		return local;
	}

	JSONArray jArrayMerge(Collection<String> a1, Collection<String> a2) {
		Set<String> r = new HashSet<String>();
		if (a1!= null)
			r.addAll(a1);
		if(a2!=null)
			r.addAll(a2);
		JSONArray result = new JSONArray();
		for(String s: r)
			result.put(s);
		return result;
	}


	public JSONArray getPeerIPs(String service, String duty) {
		JSONArray result = new JSONArray();
		JSONArray guids = getPeerGUIDs(service, duty);
		for (int i=0; i< guids.length(); i++) {
			try {
				JSONArray ips = getIPsFromGuid( guids.getString(i));
				for (int j =0; j<ips.length(); j++)
					result.put(ips.get(j));
			} catch (JSONException e) {
				logger.debug("Problem with JSON", e);
			}
		}
		logger.log(Level.DEBUG, "Service: "+service+" Duty: "+duty+" peer IPS: "+ result);
		return result;
	}


	public JSONArray getPeerNames(String service, String duty) {
		JSONArray result = new JSONArray();
		JSONArray guids = getPeerGUIDs(service, duty);
		for (int i=0; i< guids.length(); i++) {
			try {
				result.put( getAccountNamebyGUID( guids.getString(i)) );
			} catch (JSONException e) {
				logger.debug("Problem with JSON", e);
			}
			
		}
		logger.log(Level.DEBUG, "Service: "+service+" Duty: "+duty+" peer Names:: "+ result);
		return result;
	}


	public JSONArray getAccountNamebyIP(String ip) {
		JSONArray result = new JSONArray();
		JSONArray guids = getGUIDbyIP(ip);
		for (int i=0; i< guids.length(); i++) {
			try {
				result.put( getAccountNamebyGUID( guids.getString(i)) );
			} catch (JSONException e) {
				logger.debug("Problem with JSON", e);
			}
		}
		logger.log(Level.DEBUG, " Accountnames for IP: "+ip+ " Names: "+ result);
		return result;
	}
	
	//Added by Amran (Unfinished)
    //This method is written by Amran. ####################################################
    //     _                              
    //    / \   _ __ ___  _ __ __ _ _ __  
    //   / _ \ | '_ ` _ \| '__/ _` | '_ \ 
    //  / ___ \| | | | | | | | (_| | | | |
    // /_/   \_\_| |_| |_|_|  \__,_|_| |_|
	
	public JSONArray getPortNObyIP(String ip) {
		List<String> local = zkClientHandler.getPortNObyIP(ip);
		logger.log(Level.ALL, "Local query returns: "+local);
		
		List<String> global = gnsClientHandler.getPortNObyIP(ip);
			logger.log(Level.ALL, "Global query returnes: "+global);
		
		return jArrayMerge(local, global);
	}
	
	public JSONArray getIPsFromAccountName(String name) {
		String guid = getGUIDbyAccountName(name);
		return getIPsFromGuid(guid);
	}
	
//	public JSONObject metaDataRequest(JSONObject jo) {
//		return metaDataHandler.resolveRequest(jo);
//	}


	public String getZKConnString() {
		return zkClientHandler.getZKConnString();
	}


	public boolean purgeNamingCluster() {
		return zkClientHandler.purgeNamingCluster();
	}


	public JSONArray getAllLocalGUIDs() {
		return zkClientHandler.getAllLocalGUIDs();
	}


	public JSONObject readGUID(String guid) {
		return zkClientHandler.readGUID(guid);
	}


	public JSONArray getMergedGUIDs() {
		return zkClientHandler.getMergedGUIDs();
	}
}
