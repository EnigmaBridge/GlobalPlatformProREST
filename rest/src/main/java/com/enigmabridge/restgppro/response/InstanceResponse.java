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

package com.enigmabridge.restgppro.response;

import com.enigmabridge.restgppro.response.data.GeneralResponseData;
import com.enigmabridge.restgppro.response.data.InstanceResponseData;
import com.enigmabridge.restgppro.utils.CommonFnc;

/**
 * Created by Enigma Bridge Ltd (dan) on 13/01/2017.
 */
public class InstanceResponse implements GeneralResponse {
    /**
     * Result of the operation.
     * Might be JSONObject or another serializable object
     */

    private int version;
    private String error = null;
    private int status;
    private InstanceResponseData data = null;
    private String nonce = null;
    private Long latency;


    @Override
    public void setResponse(GeneralResponseData data) {
        this.data = (InstanceResponseData)data;
    }

    @Override
    public GeneralResponseData getResponse() {

        return data;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
        this.error = CommonFnc.getStatusName(status);
    }

    @Override
    public int getStatus() {
        return status;
    }


    @Override
    public void setVersion(int ver) {
        this.version = ver;
    }

    @Override
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    @Override
    public void setError(String error) {
        this.error = error;
    }

    @Override
    public void setLatency(Long length) {
        this.latency = length;
    }

    @Override
    public long getLatency() {
        return latency;
    }


    public int getVersion() {
        return version;
    }


    public String getError() {
        return error;
    }

    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    public String getNonce() {
        return this.nonce;
    }

}
