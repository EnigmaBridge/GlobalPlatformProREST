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

import com.enigmabridge.restgppro.utils.AppletStatus;

import java.util.LinkedList;

/**
 * Created by Enigma Bridge Ltd (dan) on 20/01/2017.
 */
public class CreateResponseData implements GeneralResponseData {
    private String instance;
    private String password;
    private Details detail;

    @Override
    public void setValue(Object value) {

    }

    public String getInstance() {
        return this.instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKey() {
        return this.password;
    }

    public void setDetail(int i, int size, String protocolInstance,
                          LinkedList<AppletStatus> instanceProcessors) {
        detail = new Details(i, size, protocolInstance, instanceProcessors);

    }

    public Details getDetail() {
        return detail;
    }


    public class Details {
        private int processors = 0;
        private int allocated = 0;
        private String protocol;
        private LinkedList<String> cards;

        public Details(int allocated, int size, String name, LinkedList<AppletStatus> procs) {
            this.processors = size;
            this.allocated = allocated;
            protocol = name;

            if (procs != null) {
                cards = new LinkedList<>();
                for (AppletStatus st : procs) {
                    cards.add(st.getReader());
                }
            }
        }

        public int getSize() {
            return processors;
        }

        public int getAllocated() {
            return allocated;
        }

        public String getProtocol() {
            return protocol;
        }

        public LinkedList<String> getProcessors() {
            return cards;
        }
    }
}
