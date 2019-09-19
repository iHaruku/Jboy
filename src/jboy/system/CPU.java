package jboy.system;

import jboy.instructions.Instructions;
import jboy.instructions.Instruction;

import java.util.HashMap;

/**
 * <h3>Description</h3>
 * <h5>The GameBoy uses a chip that's a cross between the Intel 8080 and the Zilog Z80. The chip is the Sharp LR35902.</h5>
 *
 * <hr/>
 * <h3>Information about the CPU</h3>
 * <ul>
 *     <li>Number of instructions: 500</li>
 *     <li>
 *         Registers
 *         <ul>
 *             <li>8-bit: A, B, C, D, E, F, H, L</li>
 *             <li>16-bit: AF, BC, DE, HL, SP, PC</li>
 *         </ul>
 *     </li>
 *     <li>Clock speed: 4.194304 MHz (4.19 MHz)</li>
 * </ul>
 *
 * <hr/>
 * <h3>A few notes on the CPU:</h3>
 * <ul>
 *     <li>Official Nintendo documents refer to "machine cycles" when describing instructions.</li>
 *     <li>One machine cycle equals four CPU clock cycles.</li>
 *     <li>The numerical value of a machine cycle is 1.048576 MHz (1.05 MHz)</li>
 * </ul>
 *
 * <hr/>
 * <h3>A few notes on the registers:</h3>
 * <ul>
 *     <li>The F register is indirectly accessible by the programmer, and is used to store the results of various math operations.</li>
 *     <li>The PC register points to the next instruction to be executed in memory.</li>
 *     <li>The SP register points to the current stack position.</li>
 *     <li>
 *         The F register consists of the following:
 *         <ul>
 *             <li>Zero flag (Z, 7th bit): This bit is set when the result of a math operation is zero or two values match when using the CP instruction.</li>
 *             <li>Subtract flag (N, 6th bit): This bit is set if a subtraction was performed in the last math instruction.</li>
 *             <li>Half carry flag (H, 5th bit): This bit is set if a carry occurred from the lower nibble in the last math operation.</li>
 *             <li>Carry flag (C, 4th bit): This bit is set if a carry occurred from the last math operation of if register A is the smaller value when executing the CP instruction.</li>
 *         </ul>
 *     </li>
 *     <li>
 *         On power up, the PC is initialized to 0x100 and the instruction at that location in the ROM is executed.
 *         From here on the PC is controlled indirectly by the instructions themselves that were generated by the programmer of the ROM cart.
 *     </li>
 *     <li>
 *         The SP is used to keep track of the top of the stack.
 *         <ul>
 *             <li>The Stack is used for saving variables, saving return addressed, passing arguments to subroutines and various other uses.</li>
 *             <li>The instructions CALL, PUSH and RST all put information onto the stack.</li>
 *             <li>The instructions POP, RET and RETI all take information off of the stack.</li>
 *             <li>Interrupts put a return address on the stack and remove it at the completion as well.</li>
 *             <li>
 *                 As information is put onto the stack, the stack grows DOWNWARD in RAM. As a result SP should always be initialized at the highest location of RAM space that has been allocated for use byu the stack.
 *                 <ul>
 *                     <li>
 *                         For example, if a programmer wants to locate the SP at the top of low RAM space (0xC000 - 0xDFFF) he would set SP to 0xE000 using LD SP,$E000.
 *                         (The SP automatically decrements before it puts something onto the stack, so it is perfectly acceptable to assign it a value which points to a memory address which is one location past the end of available RAM.)
 *                     </li>
 *                     <li>The SP is initialized to 0xFFFE on power up, but a programmer should not rely on this setting and should explicitly set its value.</li>
 *                 </ul>
 *             </li>
 *         </ul>
 *     </li>
 * </ul>
 */
public class CPU {
    public static final int FLAG_ZERO = 0x80;
    public static final int FLAG_SUB = 0x40;
    public static final int FLAG_HALF = 0x20;
    public static final int FLAG_CARRY = 0x10;

    private int A;
    private int B;
    private int C;
    private int D;
    private int E;
    private int F;
    private int H;
    private int L;
    private int AF;
    private int BC;
    private int DE;
    private int HL;
    private int SP;
    private int PC;
    private Memory memory;
    public HashMap<Integer, Instruction> instructions;

    public CPU(Memory memory) {
        this.memory = memory;
        this.PC = 0x100;
        this.SP = 0xFFFE;

        this.instructions = new HashMap<>();
        this.instructions.put(0x00, new Instruction(0x00, 1, 4, this::nop));
        this.instructions.put(0x01, new Instruction(0x01, 3, 12, this::ld_bc_nn));
        this.instructions.put(0x02, new Instruction(0x02, 1, 8, this::ld_bc_a));
        this.instructions.put(0x03, new Instruction(0x03, 1, 8, this::inc_bc));
        this.instructions.put(0x04, new Instruction(0x04, 1, 4, this::inc_c));
        this.instructions.put(0x05, new Instruction(0x05, 1, 4, this::dec_b));
        this.instructions.put(0x06, new Instruction(0x06, 2, 8, this::ld_b_n));
        this.instructions.put(0x07, new Instruction(0x07, 1, 4, this::rlca));
        this.instructions.put(0x0A, new Instruction(0x0A, 1, 8, this::ld_a_bc));
        this.instructions.put(0x3E, new Instruction(0x3E, 2, 1, this::ld_a_n));
    }

    public int getA() {
        return this.A;
    }

    public int getB() {
        return this.B;
    }

    public int getC() {
        return this.C;
    }

    public int getD() {
        return this.D;
    }

    public int getE() {
        return this.E;
    }

    public int getF() {
        return this.F;
    }

    public int getH() {
        return this.H;
    }

    public int getL() {
        return this.L;
    }

    public int getAF() {
        return this.AF;
    }

    public int getBC() {
        return this.BC;
    }

    public int getDE() {
        return this.DE;
    }

    public int getHL() {
        return this.HL;
    }

    public int getSP() {
        return this.SP;
    }

    public int getPC() {
        return this.PC;
    }

    public void setPC(int n) {
        this.PC = n;
    }

    private void incrementPC(int n) {
        this.PC += n;
    }

    public void tick() {
        Instruction instruction = this.instructions.get(this.memory.getByteAt(this.PC));
        this.execute(instruction);
    }

    private void execute(Instruction instruction) {
        switch(instruction.getOpSize()) {
            case 1:
                instruction.getOperation().apply(null);
                break;
            case 2:
                instruction.getOperation().apply(this.get8Bytes());
                break;
            case 3:
                instruction.getOperation().apply(this.get16Bytes());
                break;
        }

        this.incrementPC(instruction.getOpSize());
    }

    private int[] get8Bytes() {
        return new int[] { this.memory.getByteAt(this.PC + 1) };
    }

    private int[] get16Bytes() {
        return new int[] { this.memory.getByteAt(this.PC + 2), this.memory.getByteAt(this.PC + 1) };
    }

    private int addBytes(int highByte, int lowByte) {
        return (highByte << 8) + lowByte;
    }

    public void setFlags(int flags) {
        this.F = this.F | flags;
    }

    public void resetFlags(int flags) {
        this.F = this.F & ~flags;
    }

    /**
     * OP code 0x00 - No operation.
     * @param ops unused
     */
    private Void nop(int[] ops) {
        // nothing.
        return null;
    }

    /**
     * OP code 0x01 - Load immediate 2 bytes into BC.
     * @param ops the two immediate 8 byte chunks.
     */
    private Void ld_bc_nn(int[] ops) {
        this.BC = this.addBytes(ops[0], ops[1]);
        return null;
    }

    /**
     * OP code 0x02 - Load value of A into memory address at BC.
     * @param ops unused
     */
    private Void ld_bc_a(int[] ops) {
        this.memory.setByteAt(this.BC, this.A);
        return null;
    }

    /**
     * OP code 0x03 - Increment BC.
     * @param ops unused
     */
    private Void inc_bc(int[] ops) {
        this.BC += 1;
        return null;
    }

    /**
     * OP code 0x04 - Increment C.
     * @param ops unused
     */
    private Void inc_c(int[] ops) {
        this.C += 1;
        return null;
    }

    /**
     * OP code 0x05 - Decrement B.
     * @param ops unused
     */
    private Void dec_b(int[] ops) {
        this.B -= 1;
        return null;
    }

    /**
     * OP code 0x06 - Load immediate byte into B.
     * @param ops An 8 bit immediate value.
     */
    private Void ld_b_n(int[] ops) {
        this.B = ops[0];
        return null;
    }

    /**
     * OP code 0x07 - Shift A left by 1 bit. Carry flag is set to the 7th bit of A.
     * @param ops unused
     */
    private Void rlca(int[] ops) {
        int carry = (this.A & 0x80) >> 7;

        if(carry == 1) {
            this.setFlags(FLAG_CARRY);
        }

        // shift bit left by 1 and only keep the value below 256
        this.A = (this.A << 1) & 0xFF;

        // set the 0th bit to whatever was at the 7th bit.
        this.A = this.A | carry;
        return null;
    }

    /**
     * OP code 0x0A - Load value at memory address BC into A.
     * @param ops unused.
     */
    private Void ld_a_bc(int[] ops) {
        this.A = this.memory.getByteAt(this.BC);
        return null;
    }

    /**
     * OP code 0x0E - Load immediate byte into C.
     * @param ops An 8 bit immediate value.
     */
    private Void ld_c_n(int[] ops) {
        this.C = ops[0];
        return null;
    }

    /**
     * OP code 0x16 - Load immediate byte into D.
     * @param ops An 8 bit immediate value.
     */
    private Void ld_d_n(int[] ops) {
        this.D = ops[0];
        return null;
    }

    /**
     * OP code 0x1E - Load immediate byte into E.
     * @param ops An 8 bit immediate value.
     */
    private Void ld_e_n(int[] ops) {
        this.E = ops[0];
        return null;
    }

    /**
     * OP code 0x26 - Load immediate byte into H.
     * @param ops An 8 bit immediate value.
     */
    private Void ld_h_n(int[] ops) {
        this.H = ops[0];
        return null;
    }

    /**
     * OP code 0x2E - Load immediate byte into L.
     * @param ops An 8 bit immediate value.
     */
    private Void ld_l_n(int[] ops) {
        this.L = ops[0];
        return null;
    }

    /**
     * OP code 0x3E - Load immediate byte into A.
     * @param ops An 8 bit immediate value.
     */
    private Void ld_a_n(int[] ops) {
        this.A = ops[0];
        return null;
    }
}