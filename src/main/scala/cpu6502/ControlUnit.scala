package cpu6502

import chisel3._
import chisel3.util._
import cpu6502.CPU6502Constants._

/** ControlUnitは、CPUの制御フローを管理し、 InstructionDecoderや他のモジュールと連携して命令の実行を制御します。
  */
class ControlUnit extends Module {
  val io = IO(new Bundle {
    // メモリインターフェース
    val memDataIn = Input(UInt(8.W))
    val memDataOut = Output(UInt(8.W))
    val memAddress = Output(UInt(16.W))
    val memRead = Output(Bool())
    val memWrite = Output(Bool())

    // レジスタファイルとのインターフェース
    val regDataIn = Output(UInt(8.W))
    val regDataOut = Input(UInt(8.W))
    val regWriteEnable = Output(Bool())
    val regWriteSelect = Output(UInt(3.W))
    val regReadSelect = Output(UInt(3.W))
    val statusFlagsIn = Input(UInt(8.W))
    val statusFlagsOut = Output(UInt(8.W))
    val statusFlagsUpdate = Output(Bool())

    // ALUとのインターフェース
    val aluOperandA = Output(UInt(8.W))
    val aluOperandB = Output(UInt(8.W))
    val aluCarryIn = Output(Bool())
    val aluOperation = Output(UInt(4.W))
    val aluResult = Input(UInt(8.W))
    val aluCarryOut = Input(Bool())
    val aluZero = Input(Bool())
    val aluNegative = Input(Bool())
    val aluOverflow = Input(Bool())

    // プログラムカウンタの操作
    val pcLoad = Output(Bool())
    val pcLoadValue = Output(UInt(16.W))
    val pcIncrement = Output(Bool())
    val pcOut = Input(UInt(16.W))

    // 命令の完了信号
    val instructionComplete = Output(Bool())

    // スタックポインタの操作（必要に応じて）
    val spIncrement = Output(Bool())
    val spDecrement = Output(Bool())
  })

  // ステートマシンの定義
  val sFetch :: sDecode :: sMemory :: sExecute :: sWriteBack :: sBranch :: Nil =
    Enum(6)
  val state = RegInit(sFetch)

  // 命令レジスタとオペランドレジスタ
  val opcode = Reg(UInt(8.W))
  val operand = Reg(UInt(8.W))
  val tempAddress = Reg(UInt(16.W))

  // 命令デコーダのインスタンス化
  val decoder = Module(new InstructionDecoder)
  decoder.io.opcode := opcode
  decoder.io.statusFlags := io.statusFlagsIn

  // デフォルト値の設定
  io.memRead := false.B
  io.memWrite := false.B
  io.memAddress := 0.U
  io.memDataOut := 0.U

  io.regWriteEnable := false.B
  io.regWriteSelect := 0.U
  io.regReadSelect := 0.U
  io.regDataIn := 0.U
  io.statusFlagsOut := 0.U
  io.statusFlagsUpdate := false.B

  io.aluOperandA := 0.U
  io.aluOperandB := 0.U
  io.aluCarryIn := false.B
  io.aluOperation := 0.U

  io.pcLoad := false.B
  io.pcLoadValue := 0.U
  io.pcIncrement := false.B

  io.instructionComplete := false.B

  // 以下の2行を追加：spIncrementとspDecrementのデフォルト値を設定
  io.spIncrement := false.B
  io.spDecrement := false.B

  // メモリ読み取りデータのレジスタ
  val memDataReg = Reg(UInt(8.W))

  // ステートマシンの動作
  switch(state) {
    is(sFetch) {
      // 命令フェッチサイクル
      io.memAddress := io.pcOut
      io.memRead := true.B
      io.pcIncrement := false.B

      state := sDecode
    }
    is(sDecode) {
      // 前のクロックでのメモリデータを取得
      opcode := io.memDataIn
      io.pcIncrement := true.B

      // 命令デコードサイクル
      // アドレッシングモードに応じて次のステートを決定
      switch(decoder.io.addressMode) {
        is(ADDR_MODE_IMMEDIATE.U) {
          state := sMemory // 即値もメモリから取得
        }
        is(ADDR_MODE_ZERO_PAGE.U) {
          state := sMemory
        }
        is(ADDR_MODE_ABSOLUTE.U) {
          state := sMemory
        }
        is(ADDR_MODE_RELATIVE.U) {
          state := sMemory
        }
        is(ADDR_MODE_IMPLIED.U) {
          state := sExecute
        }
        // 他のアドレッシングモードの処理を追加
      }
    }
    is(sMemory) {
      // オペランドフェッチサイクル
      io.memAddress := io.pcOut
      io.memRead := true.B
      io.pcIncrement := false.B

      state := sExecute
    }
    is(sExecute) {
      // 前のクロックでのメモリデータを取得
      operand := io.memDataIn
      io.pcIncrement := true.B

      // ALUの設定
      io.regReadSelect := decoder.io.regReadSelect
      io.aluOperandA := io.regDataOut
      io.aluOperandB := Mux(
        decoder.io.operandBSelect === OPERAND_B_IMMEDIATE.U,
        operand,
        0.U
      )
      io.aluCarryIn := io.statusFlagsIn(FLAG_CARRY)
      io.aluOperation := decoder.io.aluOp

      // ステータスフラグの更新
      io.statusFlagsOut := Cat(
        io.aluNegative,
        io.aluOverflow,
        io.statusFlagsIn(5, 2),
        io.aluZero,
        io.aluCarryOut
      )
      io.statusFlagsUpdate := decoder.io.statusFlagUpdate

      state := sWriteBack
    }
    is(sWriteBack) {
      // 結果の書き戻し
      io.regWriteEnable := decoder.io.writeBackEnable
      io.regWriteSelect := decoder.io.writeBackSelect
      io.regDataIn := io.aluResult

      // 命令完了
      io.instructionComplete := true.B
      state := sFetch
    }
    is(sBranch) {
      // ブランチ命令の処理
      operand := io.memDataIn
      io.pcIncrement := true.B

      when(decoder.io.branchTaken) {
        // 相対アドレスを計算してPCにロード
        io.pcLoad := true.B
        io.pcLoadValue := io.pcOut + operand.asSInt.asUInt
      }
      // 命令完了
      io.instructionComplete := true.B
      state := sFetch
    }
  }
}
