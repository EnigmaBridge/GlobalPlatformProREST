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

/**
 * Base class for generic REST responses - JSON encoded.
 *
 * Created by dusanklinec on 01.08.16.
 */
public interface GeneralResponse {

    void setResponse(GeneralResponseData data);

    GeneralResponseData getResponse();

    void setStatus(int status);

    int getStatus();

    void setVersion(int version);

    void setNonce(String nonce);

    void setError(String errorString);

    void setLatency(Long length);

    public long getLatency();

}
