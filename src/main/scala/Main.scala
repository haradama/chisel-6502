package cpu6502

import chisel3._
import _root_.circt.stage.ChiselStage

object Main extends App {
  // Verilogコードの生成
  ChiselStage.emitSystemVerilogFile(
    new CPU6502,
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
  )
}
