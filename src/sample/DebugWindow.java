package sample;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jboy.other.CpuInfo;
import jboy.other.GameBoyInfo;
import jboy.other.MemoryInfo;
import jboy.system.CPU;
import jboy.system.GameBoy;
import jboy.system.Memory;

import java.util.ArrayList;
import java.util.Collections;

class DebugWindow {
    private GameBoy gameBoy;

    private HBox mainLayout;
    private VBox leftSide;
    private HBox cpuInfo;

    private Label pc;
    private Label sp;
    private Label af;
    private Label bc;
    private Label de;
    private Label hl;
    private Label interruptFlags;
    private Label interruptEnable;
    private Label ime;
    private Label lcdc;
    private Label ly;
    private Label lcdStat;

    private CheckBox z;
    private CheckBox n;
    private CheckBox h;
    private CheckBox c;

    private TextField tfBreakpoint;
    private ListView<String> breakpoints;

    private ArrayList<Integer> memoryWatch;
    private ListView<String> memoryAddresses;

    DebugWindow(GameBoy gameBoy) {
        this.gameBoy = gameBoy;

        this.mainLayout = new HBox(20);
        this.leftSide = new VBox(20);
        this.cpuInfo = new HBox(20);

        this.mainLayout.setPadding(new Insets(5, 5, 5, 5));

        this.leftSide.getChildren().add(this.cpuInfo);
        this.mainLayout.getChildren().add(this.leftSide);
    }

    HBox getLayout() {
        return this.mainLayout;
    }

    void createRegisters() {
        this.pc = new Label("PC: 0x100");
        this.sp = new Label("SP: 0xFFFE");
        this.af = new Label("AF: 0x0000");
        this.bc = new Label("BC: 0x0000");
        this.de = new Label("DE: 0x0000");
        this.hl = new Label("HL: 0x0000");
        this.interruptFlags = new Label("IF: 0x00");
        this.interruptEnable = new Label("IE: 0x00");
        this.ime = new Label("IME: off");
        this.lcdc = new Label("LCDC: 0x00");
        this.ly = new Label("LY: 0x00");
        this.lcdStat = new Label("STAT: 0x00");

        VBox vboxCpuRegisters = new VBox(
                this.af,
                this.bc,
                this.de,
                this.hl,
                this.sp,
                this.pc
        );

        VBox vboxIoRegisters = new VBox(
                this.lcdc,
                this.lcdStat,
                this.ly,
                this.interruptEnable,
                this.interruptFlags,
                this.ime
        );

        this.cpuInfo.getChildren().addAll(vboxCpuRegisters, vboxIoRegisters);
    }

    void createFlagCheckboxes() {
        this.z = new CheckBox("Z") {
            @Override
            public void arm() {
                // nothing.
            }
        };

        this.n = new CheckBox("N") {
            @Override
            public void arm() {
                // nothing.
            }
        };

        this.h = new CheckBox("H") {
            @Override
            public void arm() {
                // nothing.
            }
        };

        this.c = new CheckBox("C") {
            @Override
            public void arm() {
                // nothing.
            }
        };

        VBox vboxFlags = new VBox(z, n, h, c);
        this.cpuInfo.getChildren().add(vboxFlags);
    }

    void createMemoryControls() {
        this.memoryWatch = new ArrayList<>();
        Label lblWatchAddress = new Label("Watch address:");

        TextField tfAddressWatch = new TextField();
        tfAddressWatch.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER) {
                this.addWatchAddress(Integer.parseInt(tfAddressWatch.getText(), 16));

                tfAddressWatch.setText("");
            }
        });

        HBox hboxWatch = new HBox();
        hboxWatch.getChildren().addAll(lblWatchAddress, tfAddressWatch);

        this.memoryAddresses = new ListView<>();

        Button removeWatch = new Button("Remove watch");
        removeWatch.setOnAction(x -> this.removeWatchAddress(this.memoryAddresses.getSelectionModel().getSelectedIndex()));

        VBox vboxMemory = new VBox(hboxWatch, this.memoryAddresses, removeWatch);
        this.mainLayout.getChildren().add(vboxMemory);
    }

    private void addWatchAddress(int address) {
        this.memoryWatch.add(address);
        this.memoryWatch.sort(Collections.reverseOrder());
        this.updateWatchAddresses(this.gameBoy.getMemory());
    }

    private void removeWatchAddress(int index) {
        this.memoryWatch.remove(index);
        this.memoryWatch.sort(Collections.reverseOrder());
        this.updateWatchAddresses(this.gameBoy.getMemory());
    }

    private void updateWatchAddresses(Memory memory) {
        this.memoryAddresses.getItems().clear();

        for(Integer address : this.memoryWatch) {
            int val = memory.getByteAt(address);
            String item = "0x";
            item += String.format("%4s", Integer.toHexString(address)).toUpperCase().replace(" ", "0");
            item += ": " + String.format("%2s", Integer.toHexString(val)).toUpperCase().replace(" ", "0");
            this.memoryAddresses.getItems().add(item);
        }
    }

    void createCpuControls() {
        Button tick = new Button("Tick");
        tick.setOnAction(x -> this.gameBoy.tick());

        Button reset = new Button("Reset");
        reset.setOnAction(x -> this.gameBoy.resetCpu());

        Button run = new Button("Run to breakpoint");
        run.setOnAction(x -> this.gameBoy.runToBreakpoint());

        HBox hboxCpuControls = new HBox(10, tick, run, reset);
        this.leftSide.getChildren().add(hboxCpuControls);
    }

    void createBreakpointControls() {
        this.breakpoints = new ListView<>();

        Label lblCreateBreakpoint = new Label("Create breakpoint: ");

        this.tfBreakpoint = new TextField();
        this.tfBreakpoint.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER) {
                this.addBreakpoint(this.gameBoy);
            }
        });

        Button removeBreakpoint = new Button("Remove breakpoint");
        removeBreakpoint.setOnAction(x -> {
            int index = this.breakpoints.getSelectionModel().getSelectedIndex();

            if(index >= 0) {
                this.gameBoy.removeBreakpoint(this.breakpoints.getSelectionModel().getSelectedIndex());
            }
        });

        HBox hboxCreateBreakPoint = new HBox();
        VBox vboxBreakpointControls = new VBox();

        hboxCreateBreakPoint.getChildren().addAll(lblCreateBreakpoint, tfBreakpoint);
        vboxBreakpointControls.getChildren().addAll(
                hboxCreateBreakPoint,
                this.breakpoints,
                removeBreakpoint
        );

        this.leftSide.getChildren().add(vboxBreakpointControls);
    }

    private void addBreakpoint(GameBoy gameBoy) {
        String text = tfBreakpoint.getText();

        if(!text.isEmpty() && !text.isBlank()) {
            gameBoy.addBreakpoint(Integer.parseInt(tfBreakpoint.getText(), 16));
        }

        tfBreakpoint.setText("");
    }

    void updateWindow(GameBoyInfo info) {
        CpuInfo cpuInfo = info.getCpuInfo();
        MemoryInfo memoryInfo = info.getMemoryInfo();

        int flags = cpuInfo.getCpu().getAF();

        this.pc.setText(cpuInfo.getPC());
        this.sp.setText(cpuInfo.getSP());
        this.af.setText(cpuInfo.getAF());
        this.bc.setText(cpuInfo.getBC());
        this.de.setText(cpuInfo.getDE());
        this.hl.setText(cpuInfo.getHL());
        this.interruptFlags.setText(cpuInfo.getInterruptFlags());
        this.interruptEnable.setText(cpuInfo.getInterruptEnable());
        this.ime.setText(cpuInfo.getIME());
        this.lcdc.setText(info.getMemoryInfo().getLCDC());
        this.ly.setText(info.getMemoryInfo().getLY());
        this.lcdStat.setText(info.getMemoryInfo().getLCDStatus());

        this.z.setSelected((flags & CPU.FLAG_ZERO) == CPU.FLAG_ZERO);
        this.n.setSelected((flags & CPU.FLAG_SUB) == CPU.FLAG_SUB);
        this.h.setSelected((flags & CPU.FLAG_HALF) == CPU.FLAG_HALF);
        this.c.setSelected((flags & CPU.FLAG_CARRY) == CPU.FLAG_CARRY);

        this.breakpoints.getItems().clear();
        this.breakpoints.getItems().addAll(cpuInfo.getBreakpoints());

        this.updateWatchAddresses(memoryInfo.getMemory());
    }
}