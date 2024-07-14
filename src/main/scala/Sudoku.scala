
/* PART 0: GLOBAL / TYPE DEFINIITIONS / IMPORTS */

import scala.io.StdIn.readLine

object Globals {
  var boardsize: Int = 9
  var boxsize: Int = 3
  var cellvals: String = "123456789"
  var blank: Char = '.'
}

type Matrix[T] = List[List[T]]
type Board = Matrix[Char]
type Choices = List[Char]

/* PART 1: HELPERS */

/** Groups list into a list of lists based on the boarsize
 *
 *  @lst: list to group into smaller lists
 *  @return: list of lists of size boardsize
 */
def group[T](lst: List[T]): List[List[T]] = lst.grouped(Globals.boxsize).toList
  

/** Merges list of lists into a single List
 *
 *  @lst: list of lists to merge 
 *  @return: list of all merged elements
 */
def ungroup[T](lst: List[List[T]]): List[T] = lst.flatten


/** Checks that a list has no duplicate elements
 *
 *  @lst: list to verify for uniqueness
 *  @return: True if all elements unique else False
 */
def nodups[T](lst: List[T]): Boolean =lst match
  case Nil => true
  case x :: xs => !xs.contains(x) && nodups(xs)
  

/** Returns rows of matrix (identity of matrix)
 *
 *  @matrix: matrix to group into rows 
 *  @return: list of rows in matrix  
 */
def rows[T](matrix: Matrix[T]): Matrix[T] = matrix


/** Returns columns of matrix (Transpose of matrix)
 *
 *  @matrix: matrix to group into columns 
 *  @return: list of columns in matrix  
 */
def cols[T](matrix: Matrix[T]): Matrix[T] = matrix match
  case Nil => Nil
  case xs :: Nil => for (x <- xs) yield x::Nil
  case xs :: xss => xs.zip(cols(xss)).map((a, b) => a::b)


/** Returns a list of square boxes in matrix of the given boxsize
 *
 *  @matrix: matrix to group into boxes 
 *  @return: list of boxes 
 */
def boxs[T](matrix: Matrix[T]): Matrix[T] = ungroup(group(matrix.map(group)).map(cols)).map(ungroup)


/** Tests whether a filled board (one containing no blank characters) has different entries in each row, column and box
 *
 *  @board: Board to be tested
 *  @return: True for correct / False for incorrect
 */
def correct(board: Board): Boolean = 
  val r: Matrix[Char] = rows(board)
  val c: Matrix[Char] = cols(board)
  val b: Matrix[Char] = boxs(board)

  r.forall(nodups) && c.forall(nodups) && b.forall(nodups)


def choose(e: Char): List[Char] = if e == Globals.blank then Globals.cellvals.toList else e::Nil


def choices(board: Board): Matrix[Choices] = board.map(_.map(choose))


def cp[T](lst: List[List[T]]): List[List[T]] = lst match
  case Nil => List(Nil)
  case xs :: xss => for {
    x <- xs 
    ys <- cp(xss)
  } yield x::ys


  // FIX
def mcp[T](matrix: Matrix[List[T]]): List[Matrix[T]] = cp(matrix.map(cp))


def sudoku_v1(board: Board): List[Board] = mcp(choices(board)).filter(correct)


/* VERSION 2 */


def single[T](lst: List[T]): Boolean = lst.length == 1


def fixed(choices: List[Choices]): Choices = ungroup(choices.filter(single))


def reduce(css: List[Choices]): List[Choices] = css.map((cs: Choices) => if single(cs) then cs else cs.diff(fixed(css)))
 

def pruneBy(fn: Matrix[Choices] => Matrix[Choices]): Matrix[Choices] => Matrix[Choices] = 
  (x: Matrix[Choices]) => fn(fn(x).map(reduce))


def prune(matrix: Matrix[Choices]): Matrix[Choices] = pruneBy(boxs)(pruneBy(cols)(pruneBy(rows)(matrix)))


def sudoku_v2(board: Board): List[Board] = mcp(prune(choices(board))).filter(correct)


/* VERSION 3 */


def safe(cm: Matrix[Choices]): Boolean = rows(cm).forall((c: List[Choices]) => nodups(fixed(c))) 
  || cols(cm).forall((c: List[Choices]) => nodups(fixed(c))) 
  || boxs(cm).forall((c: List[Choices]) => nodups(fixed(c))) 


def void(cm: Matrix[Choices]): Boolean = cm.exists(_.exists(_.isEmpty))


def blocked(cm: Matrix[Choices]): Boolean = void(cm) || !safe(cm)


def minchoice(cm: Matrix[Choices]): Int = ungroup(cm.map(_.map(_.length))).filter(_ > 1).min


def expand(cm: Matrix[Choices]): List[Matrix[Choices]] = 
    val n = minchoice(cm)

    val (rows1, row::rows2) = cm.span(_.length == n): @unchecked
    val (row1, cs::row2) = row.span(_.length == n): @unchecked

    for (c <- cs) yield rows1:::List(row1:::List(c)::row2):::rows2
  

def mcp_new(matrix: Matrix[Choices]): List[Matrix[Choices]] = ungroup(expand(matrix).map(mcp_new))


def search(cm: Matrix[Choices]): List[Matrix[Choices]] = 
    if blocked(cm) then Nil 
    else if cm.forall(_.forall(single)) then List(cm)
    else ungroup(expand(cm).map((cs: Matrix[Choices]) => search(prune(cs))))


def sudoku_v3(board: Board): List[Board] = search(prune(choices(board))).map(_.map(_.map(_.head)))


def showBoard(board: Board) = board.zipWithIndex.foreach { case (row, idx) => 
  if idx % Globals.boxsize == 0 then print("\n")
  
  row.zipWithIndex.foreach { case (e, idx) => 
    if idx % Globals.boxsize == 0 then print(" ")
    print(s"$e ")
  }
  print("\n")
}


@main def sudoku(boardsize: Int, boxsize: Int, cellvals: String, blank: String, input: String): Unit =
  Globals.boardsize = boardsize
  Globals.boxsize = boxsize
  Globals.cellvals = cellvals
  Globals.blank = blank.head

  if (input == "None") then {
      println("Welcome to Sudoku Solver")
      println("Please input a Sudoku game to solve:")
      println("Example:")
      println(". . .    . . .    2 . .")
      println(". 8 .    . . 7    . 9 .")
      println("6 . 2    . . .    5 . .\n")

      println(". 7 .    . 6 .    . . .")
      println(". . .    9 . 1    . . .")
      println(". . .    . 2 .    . 4 .\n")

      println(". . 5    . . .    6 . 3")
      println(". 9 .    4 . .    . 7 .")
      println(". . 6    . . .    . . .\n")
      println("Remember to use the boardsize, boxsize, cellvals, and blank character that you input into the program.")
      println("This example is based on a basic Sudoku input.\n")

      var board: Board = Nil
      var input: Boolean = true
      var done: Boolean = false

      while input do
        println("Please input Sudoku board to solve.\n")
        board = Nil

        while board.length < Globals.boardsize do
          val row = readLine().replaceAll("\\s+", "").toList
          if row.length != Globals.boardsize then 
            println(s"Reinput row, received row size ${row.length}, expected ${Globals.boardsize}.")
          else if !(row.toSet -- (Globals.blank::Globals.cellvals.toList).toSet).isEmpty then 
            println("Reinput row, received row with extraneous character(s): " +
              s"${(row.toSet -- (Globals.blank::Globals.cellvals.toList).toSet).toList.mkString(" ")}, " +
              s"please use cellvals or blank characters only: ${(Globals.blank::Globals.cellvals.toList).mkString(" ")}")
          else 
            board = board:::List(row)

        println("\n\nThe board you inputed is:")
        showBoard(board)

        print("\nIs this correct? (Y/n): ")
        done = false
        while !done do
          val response = readLine()
          
          if response == "Y" then 
            done = true
            input = false
          else if response == "n" then
            done = true
          else
            print("Is this correct? (Y/n): ")

        showBoard(sudoku_v3(board).head)

    } else {

    }
      