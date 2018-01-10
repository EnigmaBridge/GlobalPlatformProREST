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

package com.enigmabridge.restgppro.response.data;

import java.util.HashMap;

/**
 * Created by Enigma Bridge Ltd (dan) on 20/01/2017.
 */
public class RunResponseData implements GeneralResponseData {
    private String instance;
    private Details detail;
    private int size;
    private String protocol;

    @Override
    public void setValue(Object value) {

    }

    public String getInstance() {
        return this.instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }


    public void setDetail(HashMap<String, String[][]> results, HashMap<String, Long[][]> timings) {
        detail = new Details(results, timings);

    }

    public Details getDetail() {
        return detail;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public class Details {
        private final HashMap<String, Long[][]> timing;
        private HashMap<String, String[][]> result;

        public Details(HashMap<String, String[][]> results, HashMap<String, Long[][]> timings) {
            this.result = results;
            this.timing = timings;
        }

        public HashMap<String, String[][]> getResult() {
            return result;
        }

        public HashMap<String, Long[][]> getTiming() {
            return timing;
        }

    }
}
