package cpu6502

import chisel3._
import chisel3.util._
import cpu6502.CPU6502Constants._

/** ALU（Arithmetic Logic Unit）は、算術および論理演算を行います。 このモジュールでは、6502
  * CPUがサポートする主要な演算を実装します。
  */
class ALU extends Module {
  val io = IO(new Bundle {
    // 入力オペランド
    val operandA = Input(UInt(8.W))
    val operandB = Input(UInt(8.W))
    // キャリーイン
    val carryIn = Input(Bool())
    // 演算種別
    val operation = Input(UInt(4.W))
    // 出力結果
    val result = Output(UInt(8.W))
    // ステータスフラグ
    val carryOut = Output(Bool())
    val zero = Output(Bool())
    val negative = Output(Bool())
    val overflow = Output(Bool())
  })

  // 演算結果とフラグの初期化
  val fullResult = Wire(UInt(9.W)) // キャリービットを考慮して9ビット
  fullResult := 0.U
  io.carryOut := false.B
  io.zero := false.B
  io.negative := false.B
  io.overflow := false.B

  // 演算の実装
  switch(io.operation) {
    is(ALU_OP_ADD.U) {
      val sum = Wire(UInt(9.W))
      sum := io.operandA +& io.operandB
      fullResult := sum
      io.carryOut := sum(8)
      io.overflow := (io.operandA(7) === io.operandB(7)) && (sum(7) =/= io
        .operandA(7))
    }
    is(ALU_OP_ADC.U) {
      val sum = Wire(UInt(9.W))
      sum := io.operandA +& io.operandB + io.carryIn
      fullResult := sum
      io.carryOut := sum(8)
      io.overflow := (io.operandA(7) === io.operandB(7)) && (sum(7) =/= io
        .operandA(7))
    }
    is(ALU_OP_SUB.U) {
      val diff = Wire(UInt(9.W))
      diff := io.operandA -& io.operandB
      fullResult := diff
      io.carryOut := !diff(8)
      io.overflow := (io.operandA(7) =/= io.operandB(7)) && (diff(7) =/= io
        .operandA(7))
    }
    is(ALU_OP_SBC.U) {
      val diff = Wire(UInt(9.W))
      diff := io.operandA -& io.operandB - !io.carryIn
      fullResult := diff
      io.carryOut := !diff(8)
      io.overflow := (io.operandA(7) =/= io.operandB(7)) && (diff(7) =/= io
        .operandA(7))
    }
    is(ALU_OP_AND.U) {
      fullResult := io.operandA & io.operandB
    }
    is(ALU_OP_OR.U) {
      fullResult := io.operandA | io.operandB
    }
    is(ALU_OP_EOR.U) {
      fullResult := io.operandA ^ io.operandB
    }
    is(ALU_OP_SHL.U) {
      fullResult := (io.operandA << 1)(8, 0)
      io.carryOut := io.operandA(7)
    }
    is(ALU_OP_SHR.U) {
      fullResult := (io.operandA >> 1)
      io.carryOut := io.operandA(0)
    }
    is(ALU_OP_ROL.U) {
      fullResult := Cat(io.operandA, io.carryIn)(8, 1)
      io.carryOut := io.operandA(7)
    }
    is(ALU_OP_ROR.U) {
      fullResult := Cat(io.carryIn, io.operandA)(8, 1)
      io.carryOut := io.operandA(0)
    }
    is(ALU_OP_INC.U) {
      val sum = Wire(UInt(9.W))
      sum := io.operandA +& 1.U
      fullResult := sum
      io.carryOut := sum(8)
    }
    is(ALU_OP_DEC.U) {
      val diff = Wire(UInt(9.W))
      diff := io.operandA -& 1.U
      fullResult := diff
      io.carryOut := !diff(8)
    }
    is(ALU_OP_PASS.U) {
      fullResult := io.operandA
    }
  }

  // 結果の出力（8ビットに切り詰め）
  io.result := fullResult(7, 0)

  // ステータスフラグの更新
  io.zero := io.result === 0.U
  io.negative := io.result(7)
}
