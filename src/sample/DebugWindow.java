package sample;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import goodboy.disassembler.Disassembler;
import goodboy.other.*;
import goodboy.system.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

class DebugWindow {
    private GameBoy gameBoy;

    private HBox mainLayout;
    private VBox cpuContainer;
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

    private Label timer;
    private Label timerState;
    private Label timerTacState;
    private Label timerTMA;
    private Label timerTIMA;
    private Label timerTimaClocks;
    private Label timerFrequency;

    private ListView<String> disassembly;
    private Disassembler disassembler;

    private TextField tfBreakpoint;
    private ListView<String> breakpoints;

    private ArrayList<Integer> memoryWatch;
    private ListView<String> memoryAddresses;

    DebugWindow(GameBoy gameBoy) {
        this.gameBoy = gameBoy;

        this.mainLayout = new HBox(20);
        this.cpuContainer = new VBox(20);
        this.cpuInfo = new HBox(20);

        this.mainLayout.setPadding(new Insets(5, 5, 5, 5));

        this.cpuContainer.getChildren().add(this.cpuInfo);
        this.mainLayout.getChildren().add(this.cpuContainer);
    }

    HBox getLayout() {
        return this.mainLayout;
    }

    void createRegisters() {
        this.af = new Label("AF: 0x0000");
        this.bc = new Label("BC: 0x0000");
        this.de = new Label("DE: 0x0000");
        this.hl = new Label("HL: 0x0000");
        this.pc = new Label("PC: 0x0100");
        this.sp = new Label("SP: 0xFFFE");
        this.interruptFlags = new Label("IF: 0x00");
        this.interruptEnable = new Label("IE: 0x00");
        this.ime = new Label("IME: off");
        this.lcdc = new Label("LCDC: 0x00");
        this.ly = new Label("LY: 0x00");
        this.lcdStat = new Label("STAT: 0x00");

        this.af.setFont(Font.font("monospace"));
        this.af.setMinWidth(80);
        this.bc.setFont(Font.font("monospace"));
        this.de.setFont(Font.font("monospace"));
        this.hl.setFont(Font.font("monospace"));
        this.pc.setFont(Font.font("monospace"));
        this.sp.setFont(Font.font("monospace"));
        this.lcdc.setFont(Font.font("monospace"));
        this.lcdc.setMinWidth(80);
        this.lcdStat.setFont(Font.font("monospace"));
        this.ly.setFont(Font.font("monospace"));
        this.interruptFlags.setFont(Font.font("monospace"));
        this.interruptEnable.setFont(Font.font("monospace"));
        this.ime.setFont(Font.font("monospace"));

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

    void createTimer() {
        this.timerState = new Label("Timer State: Counting");
        this.timer = new Label("Timer: 0xABCC");
        this.timerTacState = new Label("TAC: Enabled");
        this.timerTMA = new Label("TMA: 0x00");
        this.timerTIMA = new Label("TIMA: 0x00");
        this.timerTimaClocks = new Label("TIMA Clocks: 0");
        this.timerFrequency = new Label("Freq: 0Hz (0 clocks)");

        this.timerState.setFont(Font.font("monospace"));
        this.timer.setFont(Font.font("monospace"));
        this.timerTacState.setFont(Font.font("monospace"));
        this.timerTMA.setFont(Font.font("monospace"));
        this.timerTIMA.setFont(Font.font("monospace"));
        this.timerTimaClocks.setFont(Font.font("monospace"));
        this.timerFrequency.setFont(Font.font("monospace"));
        this.timerFrequency.setMinWidth(205);

        VBox vboxTimer = new VBox(
                this.timerState,
                this.timer,
                this.timerTacState,
                this.timerTMA,
                this.timerTIMA,
                this.timerTimaClocks,
                this.timerFrequency
        );

        this.cpuInfo.getChildren().add(vboxTimer);
    }

    void createCpuControls() {
        Button tick = new Button("Tick");
        tick.setOnAction(x -> this.gameBoy.tick());

        Button reset = new Button("Reset");
        reset.setOnAction(x -> this.gameBoy.reset());

        Button run = new Button("Run to breakpoint");
        run.setOnAction(x -> this.gameBoy.runToBreakpoint());

        HBox hboxCpuControls = new HBox(10, tick, run, reset);
        this.cpuContainer.getChildren().add(hboxCpuControls);
    }

    void createBreakpointControls() {
        this.breakpoints = new ListView<>();
        this.breakpoints.setStyle("-fx-font-family: \"monospace\"");
        this.breakpoints.setMaxWidth(298);
        this.breakpoints.setMaxHeight(222);

        Label lblCreateBreakpoint = new Label("Create breakpoint: ");

        this.tfBreakpoint = new TextField();
        this.tfBreakpoint.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER) {
                this.addBreakpoint();
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

        this.cpuContainer.getChildren().add(vboxBreakpointControls);
    }

    void createMemoryControls() {
        this.memoryWatch = new ArrayList<>();
        Label lblWatchAddress = new Label("Watch address:");
        lblWatchAddress.setMinWidth(105);

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
        this.memoryAddresses.setStyle("-fx-font-family: \"monospace\"");

        Button removeWatch = new Button("Remove watch");
        removeWatch.setOnAction(x -> this.removeWatchAddress(this.memoryAddresses.getSelectionModel().getSelectedIndex()));

        VBox vboxMemory = new VBox(hboxWatch, this.memoryAddresses, removeWatch);
        this.mainLayout.getChildren().add(vboxMemory);
    }

    void createDisassembly() {
        this.disassembly = new ListView<>();
        this.disassembly.setStyle("-fx-font-family: \"monospace\"");

        this.mainLayout.getChildren().add(this.disassembly);
    }

    Void updateWindow(GameBoyInfo info) {
        CpuInfo cpuInfo = info.getCpuInfo();
        MemoryInfo memoryInfo = info.getMemoryInfo();
        TimerInfo timerInfo = info.getTimerInfo();

        int flags = cpuInfo.getCpu().registers.getAF();

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

        this.z.setSelected((flags & CPU.Flags.ZERO) == CPU.Flags.ZERO);
        this.n.setSelected((flags & CPU.Flags.SUB) == CPU.Flags.SUB);
        this.h.setSelected((flags & CPU.Flags.HALF) == CPU.Flags.HALF);
        this.c.setSelected((flags & CPU.Flags.CARRY) == CPU.Flags.CARRY);

        this.timerState.setText(timerInfo.getTimerState());
        this.timer.setText(timerInfo.getTimer());
        this.timerState.setText(timerInfo.getTimerState());
        this.timerTacState.setText(timerInfo.getTacState());
        this.timerTMA.setText(timerInfo.getTMA());
        this.timerTIMA.setText(timerInfo.getTIMA());
        this.timerTimaClocks.setText(timerInfo.getTimaClocks());
        this.timerFrequency.setText(timerInfo.getFrequency());

        this.breakpoints.getItems().clear();
        this.breakpoints.getItems().addAll(cpuInfo.getBreakpoints());

        this.updateWatchAddresses(memoryInfo.getMemory());
        this.updateDisassembly();

        return null;
    }

    void tick() {
        this.gameBoy.tick();
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

    private void addBreakpoint() {
        String text = this.tfBreakpoint.getText();

        if(!text.isEmpty() && !text.isBlank()) {
            this.gameBoy.addBreakpoint(Integer.parseInt(this.tfBreakpoint.getText(), 16));
        }

        this.tfBreakpoint.setText("");
    }

    private void updateDisassembly() {
        int pc = this.gameBoy.getCpu().registers.PC;
        Disassembler disassembler = this.gameBoy.getDisassembler();

        if(disassembler != null) {
            int position = disassembler.positions.get(pc);

            if(this.disassembly.getItems().size() == 0) {
                Platform.runLater(() -> {
                    HashMap<Integer, String> disassembly = this.gameBoy.getDisassembly();

                    disassembly.forEach((index, text) -> this.disassembly.getItems().add(text));

                    this.disassembly.getSelectionModel().select(position);
                    this.disassembly.scrollTo(position - 7);
                });
            } else {
                this.disassembly.getSelectionModel().select(position);
                this.disassembly.scrollTo(position - 7);
            }
        }
    }
}
