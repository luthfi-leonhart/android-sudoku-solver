# android-sudoku-solver
A simple sudoku solver for exercise

The board view were taken from [here](https://github.com/patrickfeltes/sudoku-android-kotlin) with some modifications for the required UI.

The generator and solver algorithm were taken hrom [here](https://medium.com/@typical.dev/lets-make-the-sudoku-generator-library-in-kotlin-8e0dd45c72b6) with slight modification on generator.
On the original generator it needs to check if the whole field is solvable while removing digits one by one, which sometimes it took to long to generate (more than 5 minutes or so).
So I modified it by removing the solvable condition and let the user know if the generated board is unsolvable.

The program is written in [Kotlin](https://kotlinlang.org/) and uses [Architecture Components](https://developer.android.com/topic/libraries/architecture/).

### Generator
The generator algorithm is divided into 3 parts
#### Filling diagonal boxes
Sudoku puzzle has 3 diagonal 3x3 boxes that share neither common rows nor columns. Because of that, it’s super safe to fill them first with digits from 1 to 9 and to already have 33% of the grid completed.

#### Filling all remaining cells
Here we are iterating through all empty cells from non-diagonal boxes and taking the random digit from 1 to 9 to check if it’s safe to put it in just like in the Solver implementation until we find the correct one.

#### Removing the desired number of digits
Once we have made a grid with correctly filled digits, we have to remove some of them to complete the Sudoku puzzle. Until the desired number of digits is removed, we take the digit from the random row and column.
This is where I mode the modification from the original generator to remove solvable checker for every removal of digits.

### Solver
And the solver uses [backtracking algorithm](https://en.wikipedia.org/wiki/Sudoku_solving_algorithms)
- Place the digit "1" in the first cell and check if it is allowed to be there.
- If there are no violations (checking row, column, and box constraints) then the algorithm advances to the next cell and places a "1" in that cell.
- When checking for violations, if it is discovered that the "1" is not allowed, the value is advanced to "2".
- If a cell is discovered where none of the 9 digits is allowed, then the algorithm leaves that cell blank and moves back to the previous cell.
- The value in that cell is then incremented by one.
- This is repeated until the allowed value in the last (81st) cell is discovered.
