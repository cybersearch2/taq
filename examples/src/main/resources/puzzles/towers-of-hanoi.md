# towers-of-hanoi.taq

$ java -jar taq.jar towers-of-hanoi

```
Running query towers_of_hanoi1 in global scope 
Move disk 1 from rod A to rod C

Running query towers_of_hanoi2 in global scope 
Move disk 1 from rod A to rod B
Move disk 2 from rod A to rod C
Move disk 1 from rod B to rod C

Running query towers_of_hanoi3 in global scope 
Move disk 1 from rod A to rod C
Move disk 2 from rod A to rod B
Move disk 1 from rod C to rod B
Move disk 3 from rod A to rod C
Move disk 1 from rod B to rod A
Move disk 2 from rod B to rod C
Move disk 1 from rod A to rod C
```

### Description

towers-of-hanoi.taq demonstrates flow recursion and how to supply the initial parameters 
to start the recursion. The puzzle involves moving a stack of disks across three rods following 
some simple rules, the main one being that only one disc can be moved at a time.

Queries towers_of_hanoi 1,2 and 3 solve the puzzle for the number of discs set as a 
query parameter

> **query** towers_of_hanoi3(tower_of_hanoi)(n=3)

The first 4 terms of the "tower_of_hanoi" flow are call parameters. The first 
parameter identifies a disc. The next 3 are `from_rod, to_rod, aux_rod`, each identifying 
a different rod. The iniatial value for these parameters in supplied by template parameters 
appended to the flow

> **flow** tower_of_hanoi (\
> ...\
> )(from_rod="A", to_rod="C", aux_rod="B")

Apart from getting recursion started, template parameters provide a way to advertize 
aspects of a template that are intended to be customized, or are exprected to vary 
according to circumstances.
