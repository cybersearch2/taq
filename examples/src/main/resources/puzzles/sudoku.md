# sudoku.taq

$ java -jar taq.jar sudoku

```
Running query sudoku in global scope 
4, 1, 2, 3,
2, 3, 4, 1,
1, 2, 3, 4,
3, 4, 1, 2,
```

### Description 

sudoku.taq demonstrates numeric analysis of a 4x4 matrix to fill in the missing cells 
of a Sudoku puzzle. Two things stand out in this example

1. The query statement consists of chained stages that summarize the steps taken to 
   solve the puzzle
2. A context list across 3 different scopes (row, col, square) map out strategies for 
   navigating the matrix 
   