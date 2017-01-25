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

package com.enigmabridge.restgppro.utils;

/**
 * Created by Enigma Bridge Ltd (dan) on 18/01/2017.
 */
public class AppletStatus {

    private String reader = null;
    private byte[] appletID = null;
    private int memoryPersistent = -1;
    private int memoryReset = -1;
    private int memoryDeselect = -1;
    private Status status = Status.UNDEF;
    private String command = null;
    private String protocolInstance = null;
    private String AID;

    public void setAppletID(byte[] raw, int counter, int length) {
        this.appletID = new byte[length];
        System.arraycopy(raw, counter, appletID, 0, length);
    }

    public String getAppletID(){
        if (appletID!=null){
            StringBuilder sb = new StringBuilder(appletID.length * 2);
            for(byte b: appletID)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } else {
            return null;
        }
    }

    public void setMemoryPersistent(int memoryPersistent) {
        this.memoryPersistent = memoryPersistent;
    }

    public void setMemoryReset(int memoryReset) {
        this.memoryReset = memoryReset;
    }

    public void setMemoryDeselect(int memoryDeselect) {
        this.memoryDeselect = memoryDeselect;
    }

    public String getReader() {
        return this.reader;
    }

    public void setReader(String reader) {
        this.reader = reader;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        if (status < 1) {
            this.status = Status.ERROR;
        } else if (status == 1) {
            this.status = Status.READY;
        } else if (status == 2) {
            this.status = Status.BUSY;
        } else {
            this.status = Status.UNDEF;
        }
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String cmd) {
        command = cmd;
    }

    public void setBusy(String protocolInstance) {
        status = Status.BUSY;
        protocolInstance = protocolInstance;

    }

    public String getProtocolInstance(){
        if (status == Status.BUSY) {
            return protocolInstance;
        } else {
            return null;
        }
    }

    public void setStatusReady() {
        status = Status.READY;
    }

    public String getAID() {
        return AID;
    }

    public void setAID(String AID) {
        this.AID = AID;
    }

    public enum Status {UNDEF, ERROR, READY, BUSY}

}
