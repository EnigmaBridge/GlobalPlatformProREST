/*
 * Copyright (c) 2017 Enigma Bridge Ltd.
 *
 * This file is part of the GlobalPlatformProREST project.
 *
 *     GlobalPlatformProREST is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GlobalPlatformProREST is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GlobalPlatformProREST.  If not, see <http://www.gnu.org/licenses/>.
 *
 *     If you have any support question, use the GitHub facilities. Visit http://enigmabridge.com
 *     if you want to speak to us directly.
 */

package com.enigmabridge.restgppro.rest;

import com.enigmabridge.restgppro.response.ClientResponse;
import com.enigmabridge.restgppro.response.GeneralResponse;
import com.enigmabridge.restgppro.response.data.GeneralResponseData;
import com.enigmabridge.restgppro.utils.CommonFnc;
import com.enigmabridge.restgppro.utils.Consts;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.enigmabridge.restgppro.ApiConfig.CURRENT_API_VERSION;

/**
 * Created by Enigma Bridge Ltd (dan) on 13/01/2017.
 */
public class JsonEnvelope {

    // operations and its parameters
    public static final String UMG_REQ_OPERATIONS = "operations";

    static final String UMG_REQ_NONCE = "nonce";
    static final String UMG_REQ_VERSION = "version";
    static final String UMG_REQ_FUNCTION = "function";
    static final String UMG_REQ_ENVIRONMENT = "environment";
    static final String UMG_REQ_APIDATA = "apidata";
    static final String UMG_REQ_CLIENT = "client";
    /// endpoint - and its parameters
    static final String UMG_REQ_ENDPOINT = "endpoint";
    static final String UMG_REQ_COUNTRY = "country";
    static final String UMG_REQ_EMAIL = "email";
    static final String UMG_REQ_PRODUCTCODE = "productcode";
    static final String UMG_REQ_INSTANCETYPE = "instancetype";
    static final String UMG_REQ_NETWORK = "network";
    static final String UMG_REQ_LOCATION = "location";
    static final String UMG_REQ_APIKEY = "apikey";
    static final String UMG_REQ_IPV4 = "ipv4";
    static final String UMG_REQ_IPV6 = "ipv6";
    static final String UMG_REQ_HOSTID = "hostid";


    static final String UMG_REQ_AUTH = "authentication";
    static final String UMG_REQ_AUTH_PASSWORD = "password";
    static final String UMG_REQ_AUTH_TOTP = "totp";
    static final String UMG_REQ_AUTH_HOTP = "hotp";
    static final String UMG_REQ_AUTH_CHALLENGE = "challenge";
    static final String UMG_REQ_AUTH_SIGN = "signature";
    static final String UMG_REQ_AUTH_NAME = "name";

    static final String UMG_REQ_TYPE = "type";
    static final String UMG_REQ_TOKEN = "token";
    static final String UMG_REQ_PASSWORD = "password";
    static final String UMG_REQ_USERNAME = "username";
    static final String UMG_REQ_CHALLENGE = "challenge";
    static final String UMG_REQ_TIME = "time";

    static final String UMG_REQ_APIDATA_APIKEY = "apikey";
    static final String UMG_REQ_APIDATA_CLIENT = "username";
    static final String UMG_REQ_APIDATA_CERT = "certificate";
    static final String UMG_REQ_APIDATA_AUTH = "authentication";
    static final String UMG_REQ_APIDATA_RESPONSE = "response";

    static final String UMG_REQ_STATUSDATA_STATUS = "status";
    static final String UMG_REQ_STATUSDATA_ERROR = "error";
    static final String UMG_REQ_STATUSDATA_EMAIL = "email";
    static final String UMG_REQ_STATUSDATA_PASSWORD = "password";
    static final String UMG_REQ_STATUSDATA_DURATION = "duration";
    static final String UMG_REQ_STATUSDATA_DETAIL = "detail";
    static final String UMG_REQ_STATUSDATA_KEY = "key";


    private JSONObject jsonData;
    private GeneralResponse response;


    private JsonEnvelope() {
        response = new ClientResponse();
        response.setStatus(Consts.SW_STAT_PROCESSING_ERROR);
    }

    public static JsonEnvelope getInstance(String jsonStr) {
        JsonEnvelope newObject = new JsonEnvelope();
        try {
            JSONObject parsedContent = new JSONObject(jsonStr);
            newObject.setValue(parsedContent);
            newObject.setStatus(Consts.SW_STAT_OK);
            newObject.setResponseData(null);
        } catch (Exception ex) {
            newObject.setStatus(Consts.SW_STAT_INPUT_PARSE_FAIL);
            return null;
        }
        return newObject;
    }

    void setResponseData(GeneralResponseData resp) {
        this.response.setResponse(resp);
    }

    GeneralResponse createResponse(int code) {
        response.setVersion((int) this.jsonData.get(UMG_REQ_VERSION));
        response.setNonce((String) this.jsonData.get(UMG_REQ_NONCE));
        response.setStatus(code);
        response.setError(CommonFnc.getStatusName(code));

        return response;
    }

    public int getStatus() {
        return response.getStatus();
    }

    public void setStatus(int status) {
        this.response.setStatus(status);
    }

    public void setValue(JSONObject value) {
        this.jsonData = value;
        this.response.setVersion(CURRENT_API_VERSION);
        if (jsonData.has(UMG_REQ_NONCE)) {
            this.response.setNonce((String) jsonData.get(UMG_REQ_NONCE));
        } else {
            this.response.setNonce(null);
        }
    }

    /***
     * Returns the item - it must be String or JSON, or null if it doesn't exist.
     * @param itemName
     * @return
     */
    public JSONObject getItemJSON(String itemName) {
        if (this.jsonData.has(itemName)) {
            if (this.jsonData.get(itemName) instanceof JSONObject) {
                return (JSONObject) (this.jsonData.get(itemName));
            } else {
                return new JSONObject(this.jsonData.get(itemName));
            }
        } else {
            return null;
        }
    }

    public String getItemString(String itemName) {

        if (this.jsonData.has(itemName)) {
            Object value = this.jsonData.get(itemName);
            if (value instanceof JSONObject) {
                return value.toString();
            } else if (value instanceof String) {
                return (String) jsonData.get(itemName);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    public void setResponseCode(int responseCode) {
        this.response.setStatus(responseCode);
    }

    public GeneralResponse getResponse() {
        return response;
    }

    public void setResponseValue(String value) {
        this.response.getResponse().setValue(value);
    }

    public String getRequestString() {
        return jsonData.toString(0);
    }

    /***
     * Returns non-NULL handle only if a valid JSON array is found.
     *
     * @param itemName - JSON item name.
     * @return NULL or a handle of an JsonArray
     */
    public JSONArray getArrayJSON(String itemName) {
        if (this.jsonData.has(itemName)) {
            if (this.jsonData.get(itemName) instanceof JSONArray) {
                return this.jsonData.getJSONArray(itemName);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}