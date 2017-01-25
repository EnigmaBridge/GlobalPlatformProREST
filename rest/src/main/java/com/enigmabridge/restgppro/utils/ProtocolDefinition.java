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

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Enigma Bridge Ltd (dan) on 21/01/2017.
 */
public class ProtocolDefinition {
    private String name;
    private String AID;
    private HashMap<String, Integer> parties = new HashMap<>();
    private LinkedList<String> apduData = new LinkedList<>();
    private LinkedList<String> apduResult = new LinkedList<>();
    private HashMap<String, Instruction> apdus = new HashMap<>();
    private HashMap<String, Phase> phases = new HashMap<>();
    private Instruction initInstruction;
    private Instruction destroyInstruction;


    public ProtocolDefinition() {
        parties.put("@server", 1);
        parties.put("@worker", 0);
        apduData.add("src");
        apduData.add("dst");
        apduResult.add("src");
        apduResult.add("dst");
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAID(String AID) {
        this.AID = AID.toUpperCase();
    }

    public boolean isParty(String namein) {
        return (parties.containsKey(namein.toLowerCase()));
    }

    public boolean addData(String datain) {

        if (!apduData.contains(datain)) {
            this.apduData.add(datain);
            return apduResult.contains(datain);
        } else {
            return true;
        }

    }

    public boolean addResult(String datain) {

        if (apduResult.contains(datain)) {
            return false;
        } else {
            apduResult.add(datain);
            return true;
        }

    }


    public LinkedList<String> isDataConsistent() {

        LinkedList<String> inconsistent = new LinkedList<>();
        for (String onePiece : apduData) {
            if (!apduResult.contains(onePiece)) {
                inconsistent.add(onePiece);
            }
        }
        return inconsistent;
    }

    public boolean addInstruction(String name, String cla, String ins, String p1, String p2, String datain, String result) {
        Instruction instruction = new Instruction();
        boolean success = false;

        if ((cla.length() == 2) && (cla.matches("\\p{XDigit}+"))) {
            instruction.cls = cla.toUpperCase();
        } else {
            return false;
        }
        if ((ins.length() == 2) && (ins.matches("\\p{XDigit}+"))) {
            instruction.ins = ins.toUpperCase();
        } else {
            return false;
        }
        if (p1.startsWith("@")) {
            apduData.add(p1);
            instruction.p1 = p1;
        } else if ((p1.length() == 2) && (p1.matches("\\p{XDigit}+"))) {
            instruction.p1 = p1;
        } else {
            return false;
        }
        if (p2.startsWith("@")) {
            apduData.add(p2);
            instruction.p2 = p2;
        } else if ((p2.length() == 2) && (p2.matches("\\p{XDigit}+"))) {
            instruction.p2 = p2;
        } else {
            return false;
        }
        if (datain == null) {
            instruction.data = null;
        } else if (datain.startsWith("@")) {
            apduData.add(datain);
            instruction.data = datain;
        } else if ((datain.length() == 2) && (datain.matches("\\p{XDigit}+"))) {
            instruction.data = datain;
        } else {
            return false;
        }
        if (result == null) {
            instruction.result = null;
        } else if (result.startsWith("@")) {
            apduResult.add(result);
            instruction.result = result;
        } else {
            return false;
        }

        this.apdus.put(name, instruction);
        return true;
    }

    public void addPhase(String phaseName, String phaseResult, LinkedList<String> phaseInput) {
        Phase phase = new Phase();
        phase.result = phaseResult;
        phase.input = phaseInput;
        this.phases.put(phaseName.toLowerCase(), phase);
    }

    public void addPhaseStep(String phaseName, String apdu, String from, String to, String result) {
        PhaseStep step = new PhaseStep();

        if (this.phases.containsKey(phaseName)) {

            step.apdu = apdu;
            step.from = from;
            step.to = to;
            step.result = result;

            if (this.phases.get(phaseName.toLowerCase()) != null) {
                this.phases.get(phaseName.toLowerCase()).addStep(step);
            }

        }
    }

    public void removePhase(String phaseName) {
        if (this.phases.containsKey(phaseName)) {
            this.phases.remove(phaseName);
        }

    }

    public String getName() {
        return name;
    }


    public String getAID() {
        return AID;
    }

    public Instruction getInitInstruction() {
        return this.initInstruction;
    }

    public Instruction getDestroyInstruction() {
        return this.destroyInstruction;
    }

    public void setInitInstruction(String initInstruction) {

        if (initInstruction!=null) {
            initInstruction = initInstruction.toLowerCase();
            this.initInstruction = apdus.get(initInstruction);
        } else {
            this.initInstruction = null;
        }
    }

    public void setDestroyInstruction(String destroyInstruction) {

        if (destroyInstruction!=null) {
            destroyInstruction = destroyInstruction.toLowerCase();
            this.destroyInstruction = apdus.get(destroyInstruction);
        } else {
            this.destroyInstruction = null;
        }
    }


    private class PhaseStep {

        public String apdu;
        public String from;
        public String to;
        public String result;
    }

    private class Phase {

        public String result = null;
        public LinkedList<String> input = new LinkedList<>();
        public LinkedList<PhaseStep> steps = new LinkedList<>();

        public void addStep(PhaseStep step) {
            steps.add(step);

        }
    }

    class Instruction {

        String cls;
        String ins;
        String p1;
        String p2;
        String data;
        String result;

    }
}
