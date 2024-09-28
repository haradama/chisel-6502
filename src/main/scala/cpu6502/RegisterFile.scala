package cpu6502

import chisel3._
import chisel3.util._
import cpu6502.CPU6502Constants._

/** RegisterFileは、6502 CPUの全てのレジスタを管理します。
  */
class RegisterFile extends Module {
  val io = IO(new Bundle {
    // データバスとのインターフェース
    val dataIn = Input(UInt(8.W))
    val dataOut = Output(UInt(8.W))

    // プログラムカウンタの操作
    val pcIncrement = Input(Bool())
    val pcLoad = Input(Bool())
    val pcLoadValue = Input(UInt(16.W))
    val pcOut = Output(UInt(16.W))

    // レジスタへの書き込み制御
    val writeEnableA = Input(Bool())
    val writeEnableX = Input(Bool())
    val writeEnableY = Input(Bool())
    val writeEnableSP = Input(Bool())
    val writeEnableP = Input(Bool())

    // レジスタからの読み出し制御
    val readSelect = Input(UInt(3.W)) // 0:A, 1:X, 2:Y, 3:SP, 4:P

    // ステータスフラグの更新
    val statusFlagsIn = Input(UInt(8.W))
    val statusFlagsOut = Output(UInt(8.W))
    val statusFlagsUpdate = Input(Bool())

    // スタック操作
    val spIncrement = Input(Bool())
    val spDecrement = Input(Bool())
  })

  // レジスタの定義
  val regA = RegInit(0.U(8.W)) // アキュムレータ
  val regX = RegInit(0.U(8.W)) // インデックスレジスタX
  val regY = RegInit(0.U(8.W)) // インデックスレジスタY
  val regSP = RegInit(0xff.U(8.W)) // スタックポインタ（初期値0xFF）
  val regP = RegInit("b00100000".U(8.W)) // ステータスレジスタ
  val regPC = RegInit(0.U(16.W)) // プログラムカウンタ

  // プログラムカウンタの更新
  when(io.pcLoad) {
    regPC := io.pcLoadValue
  }.elsewhen(io.pcIncrement) {
    regPC := regPC + 1.U
  }

  io.pcOut := regPC

  // スタックポインタの更新
  when(io.spIncrement) {
    regSP := regSP + 1.U
  }.elsewhen(io.spDecrement) {
    regSP := regSP - 1.U
  }.elsewhen(io.writeEnableSP) {
    regSP := io.dataIn
  }

  // ステータスレジスタの更新
  when(io.statusFlagsUpdate) {
    // ビット5（未使用ビット）は常に1に設定
    regP := Cat(io.statusFlagsIn(7, 6), 1.U(1.W), io.statusFlagsIn(4, 0))
  }.elsewhen(io.writeEnableP) {
    regP := io.dataIn
  }

  io.statusFlagsOut := regP

  // レジスタへの書き込み
  when(io.writeEnableA) {
    regA := io.dataIn
  }

  when(io.writeEnableX) {
    regX := io.dataIn
  }

  when(io.writeEnableY) {
    regY := io.dataIn
  }

  // レジスタからの読み出し
  io.dataOut := 0.U(8.W) // デフォルト値を設定

  when(io.readSelect === REG_A.U(3.W)) {
    io.dataOut := regA
  }.elsewhen(io.readSelect === REG_X.U(3.W)) {
    io.dataOut := regX
  }.elsewhen(io.readSelect === REG_Y.U(3.W)) {
    io.dataOut := regY
  }.elsewhen(io.readSelect === REG_SP.U(3.W)) {
    io.dataOut := regSP
  }.elsewhen(io.readSelect === REG_P.U(3.W)) {
    io.dataOut := regP
  }
}
