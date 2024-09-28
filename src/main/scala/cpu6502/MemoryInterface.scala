package cpu6502

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import cpu6502.CPU6502Constants._

/** MemoryInterfaceは、CPUとメモリ（RAMおよびROM）との間のインターフェースを提供します。
  * このモジュールは、アドレスデコーダを含み、メモリマップを管理します。
  */
class MemoryInterface(romInitFile: String = "", ramInitFile: String = "")
    extends Module {
  val io = IO(new Bundle {
    // CPU側インターフェース
    val cpuAddress = Input(UInt(16.W))
    val cpuDataIn = Input(UInt(8.W))
    val cpuDataOut = Output(UInt(8.W))
    val cpuRead = Input(Bool())
    val cpuWrite = Input(Bool())
  })

  // ROMの実装
  val rom = Mem(ROM_SIZE, UInt(8.W))

  // ROMの初期化（ファイルから読み込み）
  if (romInitFile.nonEmpty) {
    loadMemoryFromFile(rom, romInitFile)
  }

  // RAMの実装
  val ram = Mem(RAM_SIZE, UInt(8.W))

  // RAMの初期化（ファイルから読み込み、必要に応じて）
  if (ramInitFile.nonEmpty) {
    loadMemoryFromFile(ram, ramInitFile)
  }

  // デフォルト値の設定
  val cpuDataOutReg = RegInit(0.U(8.W))
  io.cpuDataOut := cpuDataOutReg

  // メモリアクセスの処理
  val address = io.cpuAddress
  val readData = WireDefault(0.U(8.W))

  // メモリ読み取り
  when(io.cpuRead) {
    when(address >= RAM_START && address < (RAM_START + RAM_SIZE.U)) {
      // RAMへの読み取り
      readData := ram.read(address - RAM_START)
    }.elsewhen(address >= ROM_START && address < (ROM_START + ROM_SIZE.U)) {
      // ROMへの読み取り
      readData := rom.read(address - ROM_START)
    }.otherwise {
      // 未定義のメモリアドレス
      readData := 0xff.U
    }
  }

  // 読み取りデータをレジスタに保存
  cpuDataOutReg := readData

  // メモリ書き込み
  when(io.cpuWrite) {
    when(address >= RAM_START && address < (RAM_START + RAM_SIZE.U)) {
      // RAMへの書き込み
      ram.write(address - RAM_START, io.cpuDataIn)
    }.otherwise {
      // ROMや未定義のアドレスへの書き込みは無視
    }
  }
}
