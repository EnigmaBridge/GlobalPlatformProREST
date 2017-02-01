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

import com.enigmabridge.restgppro.utils.ProtocolInstance;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Enigma Bridge Ltd (dan) on 20/01/2017.
 */
public class InstanceResponseData implements GeneralResponseData {

    private HashMap<String, Details> details = new HashMap<>();

    @Override
    public void setValue(Object value) {

    }

    public void addInstance(String aid, ProtocolInstance instance) {

        Details detail = new Details();

        detail.setProtocol(instance.getProtocolName());
        detail.setUID(instance.getUID());
        LinkedList<String> cards = new LinkedList<>();
        for (String xx: instance.getCardKeys()){
            cards.add(instance.getCard(xx).getL().getReader());
        }
        detail.setCards(cards);
        detail.setGroupSize(instance.getCardKeys().size());
        details.put(aid, detail);
    }

    public HashMap<String, Details> getInstances() {
        return details;
    }

    public class Details {
        private LinkedList<String> cards;
        private String protocolName;
        private String UID;
        private int groupSize;

        public String getProtocol() {
            return this.protocolName;
        }

        void setProtocol(String protocolName) {
            this.protocolName = protocolName;
        }

        public String getUID() {
            return this.UID;
        }

        void setUID(String UID) {
            this.UID = UID;
        }

        public LinkedList<String> getCards() {
            return this.cards;
        }

        void setCards(LinkedList<String> cards) {
            this.cards = cards;
        }

        public int getSize() {
            return this.groupSize;
        }

        void setGroupSize(int groupSize) {
            this.groupSize = groupSize;
        }

    }
}
