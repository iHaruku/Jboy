package test.jboy.system;

import jboy.system.CPU;
import jboy.system.Memory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CPUInstructions0x10_0x1F {
    static private CPU cpu;
    static private Memory memory;
    static private int[] rom;

    @BeforeAll
    static void testBeforeAll() {
        memory = new Memory();
        cpu = new CPU(memory);
    }

    @BeforeEach
    void setUp() {
        cpu.setPC(0x100);
        rom = new int[0x7FFF];
    }

    @AfterEach
    void tearDown() {
        rom = null;
        cpu.resetFlags(CPU.FLAG_ZERO | CPU.FLAG_SUB | CPU.FLAG_HALF | CPU.FLAG_CARRY);
    }

    // op code 0x10
    @Test
    void stop_test() {}

    // op code 0x11
    @Test
    void ld_de_xx_test() {
        rom[0x100] = 0x11; // ld de,0x895F
        rom[0x101] = 0x5F;
        rom[0x102] = 0x89;

        memory.loadROM(rom);

        cpu.tick();
        assertEquals(0x895F, cpu.getDE(), "The DE register should equal 0x895F.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");
    }

    // op code 0x12
    @Test
    void ld_dep_a_test() {}

    // op code 0x13
    @Test
    void inc_de_test() {}

    // op code 0x14
    @Test
    void inc_d_test() {
        rom[0x100] = 0x16; // ld d,0x00
        rom[0x101] = 0x00;
        rom[0x102] = 0x14; // inc d

        memory.loadROM(rom);

        cpu.tick();
        cpu.tick();
        assertEquals(0x01, cpu.getD(), "The D register should equal 0x01.");
        assertEquals(0x00, cpu.getF(), "No flags should be set.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");

        cpu.setPC(0x100);
        cpu.resetFlags(CPU.FLAG_ZERO | CPU.FLAG_SUB | CPU.FLAG_HALF | CPU.FLAG_CARRY);

        rom[0x100] = 0x16; // ld d,0x0F
        rom[0x101] = 0x0F;
        rom[0x102] = 0x14; // inc d

        cpu.tick();
        cpu.tick();
        assertEquals(0x10, cpu.getD(), "The D register should equal 0x10.");
        assertEquals(CPU.FLAG_HALF, cpu.getF(), "The HALF_CARRY flag should be set.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");

        cpu.setPC(0x100);
        cpu.resetFlags(CPU.FLAG_ZERO | CPU.FLAG_SUB | CPU.FLAG_HALF | CPU.FLAG_CARRY);

        rom[0x100] = 0x16; // ld d,0xFF
        rom[0x101] = 0xFF;
        rom[0x102] = 0x14; // inc d

        cpu.tick();
        cpu.tick();
        assertEquals(0x00, cpu.getD(), "The D register should equal 0x00.");
        assertEquals(CPU.FLAG_ZERO | CPU.FLAG_HALF, cpu.getF(), "The ZERO and HALF_CARRY flags should be set.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");
    }

    // op code 0x15
    @Test
    void dec_d_test() {
        rom[0x100] = 0x16; // ld d,0xFF
        rom[0x101] = 0xFF;
        rom[0x102] = 0x15; // dec d

        memory.loadROM(rom);

        cpu.tick();
        cpu.tick();
        assertEquals(0xFE, cpu.getD(), "The D register should equal 0xFE.");
        assertEquals(CPU.FLAG_SUB, cpu.getF(), "The SUB flag should be set.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");

        // Test a decrement that results in zero.
        cpu.setPC(0x100);
        cpu.resetFlags(CPU.FLAG_ZERO | CPU.FLAG_SUB | CPU.FLAG_HALF | CPU.FLAG_CARRY);
        rom[0x100] = 0x16; // ld d,0x01
        rom[0x101] = 0x01;
        rom[0x102] = 0x15; // dec d

        memory.loadROM(rom);

        cpu.tick();
        cpu.tick();
        assertEquals(0x00, cpu.getD(), "The D register should equal 0x00.");
        assertEquals(CPU.FLAG_ZERO | CPU.FLAG_SUB, cpu.getF(), "ZERO and SUB flags should be set.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");

        // Test a half carry.
        cpu.setPC(0x100);
        cpu.resetFlags(CPU.FLAG_ZERO | CPU.FLAG_SUB | CPU.FLAG_HALF | CPU.FLAG_CARRY);
        rom[0x100] = 0x16; // ld d,0x10
        rom[0x101] = 0x10;
        rom[0x102] = 0x15; // dec d

        memory.loadROM(rom);

        cpu.tick();
        cpu.tick();
        assertEquals(0x0F, cpu.getD(), "The D register should equal 0x0F.");
        assertEquals(CPU.FLAG_SUB | CPU.FLAG_HALF, cpu.getF(), "The SUB and HALF_CARRY flags should be set.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");

        // Test decrementing zero.
        cpu.setPC(0x100);
        cpu.resetFlags(CPU.FLAG_ZERO | CPU.FLAG_SUB | CPU.FLAG_HALF | CPU.FLAG_CARRY);
        rom[0x100] = 0x16; // ld d,0x00
        rom[0x101] = 0x00;
        rom[0x102] = 0x15; // dec d

        memory.loadROM(rom);

        cpu.tick();
        cpu.tick();
        assertEquals(0xFF, cpu.getD(), "The D register should equal 0xFF.");
        assertEquals(CPU.FLAG_SUB | CPU.FLAG_HALF, cpu.getF(), "The SUB and HALF_CARRY flags should be set.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");
    }

    // op code 0x16
    @Test
    void ld_d_x_test() {
        rom[0x100] = 0x16; // ld d,0xCD
        rom[0x101] = 0xCD;

        memory.loadROM(rom);

        cpu.tick();
        assertEquals(0xCD, cpu.getD(), "The D register should equal 0xCD.");
        assertEquals(0x102, cpu.getPC(), "PC should equal 0x102.");
    }

    // op code 0x17
    @Test
    void rla_test() {}

    // op code 0x18
    @Test
    void jr_x_test() {}

    // op code 0x19
    @Test
    void add_hl_de_test() {}

    // op code 0x1A
    @Test
    void ld_a_dep_test() {}

    // op code 0x1B
    @Test
    void dec_de_test() {}

    // op code 0x1C
    @Test
    void inc_e_test() {
        rom[0x100] = 0x1E; // ld e,0x00
        rom[0x101] = 0x00;
        rom[0x102] = 0x1C; // inc e

        memory.loadROM(rom);

        cpu.tick();
        cpu.tick();
        assertEquals(0x01, cpu.getE(), "The E register should equal 0x01.");
        assertEquals(0x00, cpu.getF(), "No flags should be set.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");

        cpu.setPC(0x100);
        cpu.resetFlags(CPU.FLAG_ZERO | CPU.FLAG_SUB | CPU.FLAG_HALF | CPU.FLAG_CARRY);

        rom[0x100] = 0x1E; // ld e,0x0F
        rom[0x101] = 0x0F;
        rom[0x102] = 0x1C; // inc e

        cpu.tick();
        cpu.tick();
        assertEquals(0x10, cpu.getE(), "The E register should equal 0x10.");
        assertEquals(CPU.FLAG_HALF, cpu.getF(), "The HALF_CARRY flag should be set.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");

        cpu.setPC(0x100);
        cpu.resetFlags(CPU.FLAG_ZERO | CPU.FLAG_SUB | CPU.FLAG_HALF | CPU.FLAG_CARRY);

        rom[0x100] = 0x1E; // ld e,0xFF
        rom[0x101] = 0xFF;
        rom[0x102] = 0x1C; // inc e

        cpu.tick();
        cpu.tick();
        assertEquals(0x00, cpu.getE(), "The E register should equal 0x00.");
        assertEquals(CPU.FLAG_ZERO | CPU.FLAG_HALF, cpu.getF(), "The ZERO and HALF_CARRY flags should be set.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");
    }

    // op code 0x1D
    @Test
    void dec_e_test() {
        rom[0x100] = 0x1E; // ld e,0xFF
        rom[0x101] = 0xFF;
        rom[0x102] = 0x1D; // dec e

        memory.loadROM(rom);

        cpu.tick();
        cpu.tick();
        assertEquals(0xFE, cpu.getE(), "The E register should equal 0xFE.");
        assertEquals(CPU.FLAG_SUB, cpu.getF(), "The SUB flag should be set.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");

        // Test a decrement that results in zero.
        cpu.setPC(0x100);
        cpu.resetFlags(CPU.FLAG_ZERO | CPU.FLAG_SUB | CPU.FLAG_HALF | CPU.FLAG_CARRY);
        rom[0x100] = 0x1E; // ld e,0x01
        rom[0x101] = 0x01;
        rom[0x102] = 0x1D; // dec e

        memory.loadROM(rom);

        cpu.tick();
        cpu.tick();
        assertEquals(0x00, cpu.getE(), "The E register should equal 0x00.");
        assertEquals(CPU.FLAG_ZERO | CPU.FLAG_SUB, cpu.getF(), "ZERO and SUB flags should be set.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");

        // Test a half carry.
        cpu.setPC(0x100);
        cpu.resetFlags(CPU.FLAG_ZERO | CPU.FLAG_SUB | CPU.FLAG_HALF | CPU.FLAG_CARRY);
        rom[0x100] = 0x1E; // ld e,0x10
        rom[0x101] = 0x10;
        rom[0x102] = 0x1D; // dec e

        memory.loadROM(rom);

        cpu.tick();
        cpu.tick();
        assertEquals(0x0F, cpu.getE(), "The E register should equal 0x0F.");
        assertEquals(CPU.FLAG_SUB | CPU.FLAG_HALF, cpu.getF(), "The SUB and HALF_CARRY flags should be set.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");

        // Test decrementing zero.
        cpu.setPC(0x100);
        cpu.resetFlags(CPU.FLAG_ZERO | CPU.FLAG_SUB | CPU.FLAG_HALF | CPU.FLAG_CARRY);
        rom[0x100] = 0x1E; // ld e,0x00
        rom[0x101] = 0x00;
        rom[0x102] = 0x1D; // dec e

        memory.loadROM(rom);

        cpu.tick();
        cpu.tick();
        assertEquals(0xFF, cpu.getE(), "The E register should equal 0xFF.");
        assertEquals(CPU.FLAG_SUB | CPU.FLAG_HALF, cpu.getF(), "The SUB and HALF_CARRY flags should be set.");
        assertEquals(0x103, cpu.getPC(), "PC should equal 0x103.");
    }

    // op code 0x1E
    @Test
    void ld_e_x_test() {
        rom[0x100] = 0x1E; // ld e,0xCD
        rom[0x101] = 0xCD;

        memory.loadROM(rom);

        cpu.tick();
        assertEquals(0xCD, cpu.getE(), "The E register should equal 0xCD.");
        assertEquals(0x102, cpu.getPC(), "PC should equal 0x102.");
    }

    // op code 0x1F
    @Test
    void rra_test() {}
}