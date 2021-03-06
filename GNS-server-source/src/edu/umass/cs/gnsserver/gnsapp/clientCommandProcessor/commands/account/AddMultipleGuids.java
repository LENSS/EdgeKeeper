/*
 *
 *  Copyright (c) 2015 University of Massachusetts
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you
 *  may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 *  Initial developer(s): Westy
 *
 */
package edu.umass.cs.gnsserver.gnsapp.clientCommandProcessor.commands.account;

import edu.umass.cs.gnsserver.gnsapp.clientCommandProcessor.ClientRequestHandlerInterface;
import edu.umass.cs.gnsserver.gnsapp.clientCommandProcessor.commandSupport.AccountAccess;
import edu.umass.cs.gnsserver.gnsapp.clientCommandProcessor.commandSupport.AccountInfo;
import edu.umass.cs.gnsserver.gnsapp.clientCommandProcessor.commandSupport.CommandResponse;
import edu.umass.cs.gnsserver.gnsapp.clientCommandProcessor.commandSupport.GuidInfo;
import edu.umass.cs.gnsserver.gnsapp.clientCommandProcessor.commands.CommandModule;
import edu.umass.cs.gnsserver.gnsapp.clientCommandProcessor.commands.AbstractCommand;
import edu.umass.cs.gnscommon.CommandType;
import edu.umass.cs.gnscommon.ResponseCode;
import edu.umass.cs.gnsserver.gnsapp.clientSupport.NSAccessSupport;
import edu.umass.cs.gnsserver.interfaces.InternalRequestHeader;
import edu.umass.cs.gnsserver.main.GNSConfig;
import edu.umass.cs.gnsserver.utils.JSONUtils;
import edu.umass.cs.utils.Config;
import edu.umass.cs.utils.Util;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.gnscommon.GNSProtocol;
import edu.umass.cs.gnscommon.packets.CommandPacket;

import java.util.Set;
import java.util.logging.Level;

/**
 * Command to add a multiple guids using batch support.
 *
 * @author westy
 */
public class AddMultipleGuids extends AbstractCommand {

  /**
   * Creates an AddGuid instance.
   *
   * @param module
   */
  public AddMultipleGuids(CommandModule module) {
    super(module);
  }

  /**
   *
   * @return the command type
   */
  @Override
  public CommandType getCommandType() {
    return CommandType.AddMultipleGuids;
  }

  @Override
  public CommandResponse execute(InternalRequestHeader header, CommandPacket commandPacket, ClientRequestHandlerInterface handler) throws InvalidKeyException, InvalidKeySpecException,
          JSONException, NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException {
    JSONObject json = commandPacket.getCommand();
    String guid = json.getString(GNSProtocol.GUID.toString());
    String guidCntString = json.optString(GNSProtocol.GUIDCNT.toString());
    JSONArray names = json.optJSONArray(GNSProtocol.NAMES.toString());
    JSONArray publicKeys = json.optJSONArray(GNSProtocol.PUBLIC_KEYS.toString());
    String signature = json.getString(GNSProtocol.SIGNATURE.toString());
    String message = json.getString(GNSProtocol.SIGNATUREFULLMESSAGE.toString());
    
    
    Set<InetSocketAddress> activesSet = json.has
    	(GNSProtocol.ACTIVES_SET.toString())? Util.getSocketAddresses
    			(json.getJSONArray(GNSProtocol.ACTIVES_SET.toString())): null;
    
    //aditya: not supported yet, because there is no suitable public CreateServiceName constructor 
    // to do this.
	if(activesSet != null)
	{
		throw new RuntimeException("Batch creation with custom set of actives is not "
				+ "supported yet.");
	}

    GuidInfo accountGuidInfo;
    if ((accountGuidInfo = AccountAccess.lookupGuidInfoAnywhere(header,guid, handler)) == null) {
      return new CommandResponse(ResponseCode.BAD_GUID_ERROR, GNSProtocol.BAD_RESPONSE.toString() + " " + GNSProtocol.BAD_GUID.toString() + " " + guid);
    }
    if (NSAccessSupport.verifySignature(accountGuidInfo.getPublicKey(), signature, message)) {
      AccountInfo accountInfo = AccountAccess.lookupAccountInfoFromGuidAnywhere(header, guid, handler);
      if (accountInfo == null) {
        return new CommandResponse(ResponseCode.BAD_ACCOUNT_ERROR, GNSProtocol.BAD_RESPONSE.toString() + " " + GNSProtocol.BAD_ACCOUNT.toString() + " " + guid);
      }
      if (!accountInfo.isVerified()) {
        return new CommandResponse(ResponseCode.VERIFICATION_ERROR, GNSProtocol.BAD_RESPONSE.toString() + " " + GNSProtocol.VERIFICATION_ERROR.toString() + " Account not verified");
      } else if (accountInfo.getGuids().size() > Config.getGlobalInt(GNSConfig.GNSC.ACCOUNT_GUID_MAX_SUBGUIDS)) {
        return new CommandResponse(ResponseCode.TOO_MANY_GUIDS_EXCEPTION, GNSProtocol.BAD_RESPONSE.toString() + " " + GNSProtocol.TOO_MANY_GUIDS.toString());
      } else if (names != null && publicKeys != null) {
        GNSConfig.getLogger().log(Level.INFO, "ADD SLOW{0} / {1}", new Object[]{names, publicKeys});
        return AccountAccess.addMultipleGuids(header, commandPacket, JSONUtils.JSONArrayToArrayListString(names),
                JSONUtils.JSONArrayToArrayListString(publicKeys),
                accountInfo, accountGuidInfo, handler);
      } else if (names != null) {
        //GNS.getLogger().info("ADD FASTER" + names + " / " + publicKeys);
        return AccountAccess.addMultipleGuidsFaster(header, commandPacket, JSONUtils.JSONArrayToArrayListString(names),
                accountInfo, accountGuidInfo, handler);
      } else if (guidCntString != null) {
        //GNS.getLogger().info("ADD RANDOM" + names + " / " + publicKeys);
        int guidCnt = Integer.parseInt(guidCntString);
        return AccountAccess.addMultipleGuidsFasterAllRandom(header, commandPacket, guidCnt, accountInfo, accountGuidInfo, handler);
      } else {
        return new CommandResponse(ResponseCode.UNSPECIFIED_ERROR, GNSProtocol.BAD_RESPONSE.toString() + " " + GNSProtocol.UNSPECIFIED_ERROR.toString()
                + " bad arguments: need " + GNSProtocol.NAMES.toString() + " or " + GNSProtocol.NAMES.toString() + " and " + GNSProtocol.PUBLIC_KEYS.toString() + " or " + GNSProtocol.GUIDCNT.toString());
      }
    } else {
      return new CommandResponse(ResponseCode.SIGNATURE_ERROR, GNSProtocol.BAD_RESPONSE.toString() + " " + GNSProtocol.BAD_SIGNATURE.toString());
    }
    //}
  }

}
