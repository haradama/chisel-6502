package cpu6502

import chisel3._
import chisel3.util._

/** CPU6502Constantsは、6502 CPUで使用する共通の定数を定義します。
  * これにより、各モジュールで定数を一元管理し、コードの重複を防ぎます。
  */
object CPU6502Constants {
  // ALU操作コードの定義
  val ALU_OP_ADC = 0
  val ALU_OP_AND = 1
  val ALU_OP_OR = 2
  val ALU_OP_EOR = 3
  val ALU_OP_ADD = 4
  val ALU_OP_SUB = 5
  val ALU_OP_SBC = 6
  val ALU_OP_INC = 7
  val ALU_OP_DEC = 8
  val ALU_OP_PASS = 9
  val ALU_OP_SHL = 10
  val ALU_OP_SHR = 11
  val ALU_OP_ROL = 12
  val ALU_OP_ROR = 13
  val ALU_OP_XOR = 14
  // 必要に応じて他の操作コードを追加

  // アドレッシングモードの定義
  val ADDR_MODE_IMMEDIATE = 0
  val ADDR_MODE_ZERO_PAGE = 1
  val ADDR_MODE_ABSOLUTE = 2
  val ADDR_MODE_RELATIVE = 3
  val ADDR_MODE_IMPLIED = 4
  val ADDR_MODE_ACCUMULATOR = 5
  val ADDR_MODE_INDEXED_X = 6
  val ADDR_MODE_INDEXED_Y = 7
  // 必要に応じて他のアドレッシングモードを追加

  // オペランド選択の定義
  val OPERAND_A_ACCUMULATOR = 0
  val OPERAND_A_REGISTER_X = 1
  val OPERAND_A_REGISTER_Y = 2
  val OPERAND_A_MEMORY = 3

  val OPERAND_B_IMMEDIATE = 0
  val OPERAND_B_MEMORY = 1
  val OPERAND_B_ZERO = 2

  // レジスタ選択の定義
  val REG_A = 0
  val REG_X = 1
  val REG_Y = 2
  val REG_SP = 3
  val REG_P = 4
  // 必要に応じて他のレジスタを追加

  // ステータスフラグのビット位置
  val FLAG_CARRY = 0
  val FLAG_ZERO = 1
  val FLAG_INTERRUPT_DISABLE = 2
  val FLAG_DECIMAL_MODE = 3
  val FLAG_BREAK = 4
  val FLAG_UNUSED = 5
  val FLAG_OVERFLOW = 6
  val FLAG_NEGATIVE = 7

  // メモリマップの定義
  val ROM_START = 0xe000.U(16.W)
  val ROM_SIZE = 0x2000 // 8KB
  val RAM_START = 0x0000.U(16.W)
  val RAM_SIZE = 0x0800 // 2KB

  // その他必要な定数をここに追加
}
