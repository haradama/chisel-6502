package cpu6502

import chisel3._
import chisel3.util._
import cpu6502.CPU6502Constants._

/** CPU6502は、6502 CPUの全てのコンポーネントを統合するトップレベルモジュールです。
  * ALU、RegisterFile、ControlUnit、MemoryInterfaceをインスタンス化し、適切に接続します。
  */
class CPU6502(romInitFile: String = "", ramInitFile: String = "")
    extends Module {
  val io = IO(new Bundle {
    // デバッグ用の出力（必要に応じて）
    // val debug = Output(new Bundle {
    //   val pc = UInt(16.W)
    //   val a = UInt(8.W)
    //   val x = UInt(8.W)
    //   val y = UInt(8.W)
    //   val sp = UInt(8.W)
    //   val p = UInt(8.W)
    // })
  })

  // 各モジュールのインスタンス化
  val controlUnit = Module(new ControlUnit)
  val registerFile = Module(new RegisterFile)
  val alu = Module(new ALU)
  val memoryInterface = Module(new MemoryInterface(romInitFile, ramInitFile))

  // ControlUnitとMemoryInterfaceの接続
  controlUnit.io.memDataIn := memoryInterface.io.cpuDataOut
  memoryInterface.io.cpuDataIn := controlUnit.io.memDataOut
  memoryInterface.io.cpuAddress := controlUnit.io.memAddress
  memoryInterface.io.cpuRead := controlUnit.io.memRead
  memoryInterface.io.cpuWrite := controlUnit.io.memWrite

  // ControlUnitとRegisterFileの接続
  controlUnit.io.regDataOut := registerFile.io.dataOut
  registerFile.io.dataIn := controlUnit.io.regDataIn

  // レジスタ書き込みの制御信号の接続
  registerFile.io.writeEnableA := controlUnit.io.regWriteEnable && (controlUnit.io.regWriteSelect === REG_A.U)
  registerFile.io.writeEnableX := controlUnit.io.regWriteEnable && (controlUnit.io.regWriteSelect === REG_X.U)
  registerFile.io.writeEnableY := controlUnit.io.regWriteEnable && (controlUnit.io.regWriteSelect === REG_Y.U)
  registerFile.io.writeEnableSP := controlUnit.io.regWriteEnable && (controlUnit.io.regWriteSelect === REG_SP.U)
  registerFile.io.writeEnableP := controlUnit.io.regWriteEnable && (controlUnit.io.regWriteSelect === REG_P.U)

  registerFile.io.readSelect := controlUnit.io.regReadSelect

  // ステータスフラグの接続
  controlUnit.io.statusFlagsIn := registerFile.io.statusFlagsOut
  registerFile.io.statusFlagsIn := controlUnit.io.statusFlagsOut
  registerFile.io.statusFlagsUpdate := controlUnit.io.statusFlagsUpdate

  // ALUの接続
  alu.io.operandA := controlUnit.io.aluOperandA
  alu.io.operandB := controlUnit.io.aluOperandB
  alu.io.carryIn := controlUnit.io.aluCarryIn
  alu.io.operation := controlUnit.io.aluOperation

  controlUnit.io.aluResult := alu.io.result
  controlUnit.io.aluCarryOut := alu.io.carryOut
  controlUnit.io.aluZero := alu.io.zero
  controlUnit.io.aluNegative := alu.io.negative
  controlUnit.io.aluOverflow := alu.io.overflow

  // プログラムカウンタの接続
  controlUnit.io.pcOut := registerFile.io.pcOut
  registerFile.io.pcIncrement := controlUnit.io.pcIncrement
  registerFile.io.pcLoad := controlUnit.io.pcLoad
  registerFile.io.pcLoadValue := controlUnit.io.pcLoadValue

  // スタックポインタの操作
  registerFile.io.spIncrement := controlUnit.io.spIncrement
  registerFile.io.spDecrement := controlUnit.io.spDecrement

  // デバッグ用の出力（必要に応じて）
  // io.debug.pc := registerFile.io.pcOut
  // io.debug.a := registerFile.io.debugA
  // io.debug.x := registerFile.io.debugX
  // io.debug.y := registerFile.io.debugY
  // io.debug.sp := registerFile.io.debugSP
  // io.debug.p := registerFile.io.debugP
}
