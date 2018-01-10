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

import com.enigmabridge.restgppro.response.data.DestroyResponseData;
import com.enigmabridge.restgppro.response.data.GeneralResponseData;
import com.enigmabridge.restgppro.utils.Consts;

/**
 * Created by Enigma Bridge Ltd (dan) on 20/01/2017.
 */
public class DestroyResponse implements GeneralResponse {
    private DestroyResponseData data = null;
    private int status = Consts.SW_STAT_OK;
    private long latency;

    @Override
    public void setResponse(GeneralResponseData data) {
        this.data = (DestroyResponseData) data;

    }

    @Override
    public GeneralResponseData getResponse() {
        return data;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;

    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setVersion(int version) {

    }

    @Override
    public void setNonce(String nonce) {

    }

    @Override
    public void setError(String errorString) {

    }

    @Override
    public void setLatency(Long length) {
        this.latency = length;
    }

    @Override
    public long getLatency() {
        return latency;
    }
}
