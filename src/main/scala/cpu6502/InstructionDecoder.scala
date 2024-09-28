package cpu6502

import chisel3._
import chisel3.util._
import cpu6502.CPU6502Constants._

/** InstructionDecoderは、フェッチされた命令オペコードをデコードし、 必要な制御信号を生成します。
  */
class InstructionDecoder extends Module {
  val io = IO(new Bundle {
    // フェッチされたオペコード
    val opcode = Input(UInt(8.W))
    // ステータスレジスタからのフラグ
    val statusFlags = Input(UInt(8.W))

    // デコードされた制御信号
    val aluOp = Output(UInt(4.W)) // ALUの操作コード
    val operandASelect = Output(UInt(2.W)) // オペランドAの選択
    val operandBSelect = Output(UInt(2.W)) // オペランドBの選択
    val writeBackEnable = Output(Bool()) // 結果の書き戻し許可
    val writeBackSelect = Output(UInt(3.W)) // 書き戻し先のレジスタ選択
    val memoryRead = Output(Bool()) // メモリ読み取り信号
    val memoryWrite = Output(Bool()) // メモリ書き込み信号
    val addressMode = Output(UInt(3.W)) // アドレッシングモード
    val programCounterIncrement = Output(Bool()) // プログラムカウンタのインクリメント
    val branchTaken = Output(Bool()) // ブランチの有無
    val statusFlagUpdate = Output(Bool()) // ステータスフラグの更新
    val instructionComplete = Output(Bool()) // 命令の完了
    val regReadSelect = Output(UInt(3.W)) // 読み出し元のレジスタ選択
    val regWriteSelect = Output(UInt(3.W)) // 書き込み先のレジスタ選択
  })

  // ゼロフラグの抽出
  val zeroFlag = io.statusFlags(FLAG_ZERO)
  val negativeFlag = io.statusFlags(FLAG_NEGATIVE)
  val carryFlag = io.statusFlags(FLAG_CARRY)

  // デフォルト値の設定
  io.aluOp := ALU_OP_PASS.U
  io.operandASelect := OPERAND_A_ACCUMULATOR.U
  io.operandBSelect := OPERAND_B_ZERO.U
  io.writeBackEnable := false.B
  io.writeBackSelect := REG_A.U
  io.memoryRead := false.B
  io.memoryWrite := false.B
  io.addressMode := ADDR_MODE_IMPLIED.U
  io.programCounterIncrement := false.B
  io.branchTaken := false.B
  io.statusFlagUpdate := false.B
  io.instructionComplete := false.B
  io.regReadSelect := REG_A.U
  io.regWriteSelect := REG_A.U

  // 命令デコードの実装
  switch(io.opcode) {
    is("h69".U) { // ADC Immediate
      io.aluOp := ALU_OP_ADC.U
      io.operandASelect := OPERAND_A_ACCUMULATOR.U
      io.operandBSelect := OPERAND_B_IMMEDIATE.U
      io.writeBackEnable := true.B
      io.writeBackSelect := REG_A.U
      io.addressMode := ADDR_MODE_IMMEDIATE.U
      io.statusFlagUpdate := true.B
      io.regReadSelect := REG_A.U
    }
    is("h65".U) { // ADC Zero Page
      io.aluOp := ALU_OP_ADC.U
      io.operandASelect := OPERAND_A_ACCUMULATOR.U
      io.operandBSelect := OPERAND_B_MEMORY.U
      io.writeBackEnable := true.B
      io.writeBackSelect := REG_A.U
      io.memoryRead := true.B
      io.addressMode := ADDR_MODE_ZERO_PAGE.U
      io.statusFlagUpdate := true.B
      io.regReadSelect := REG_A.U
    }
    is("hE9".U) { // SBC Immediate
      io.aluOp := ALU_OP_SBC.U
      io.operandASelect := OPERAND_A_ACCUMULATOR.U
      io.operandBSelect := OPERAND_B_IMMEDIATE.U
      io.writeBackEnable := true.B
      io.writeBackSelect := REG_A.U
      io.addressMode := ADDR_MODE_IMMEDIATE.U
      io.statusFlagUpdate := true.B
      io.regReadSelect := REG_A.U
    }
    // 他の命令のデコードをここに追加

    // BNE命令の例
    is("hD0".U) { // BNE (Branch if Not Equal)
      io.addressMode := ADDR_MODE_RELATIVE.U
      when(zeroFlag === 0.U) {
        io.branchTaken := true.B
      }
      io.statusFlagUpdate := false.B
    }
  }
}
