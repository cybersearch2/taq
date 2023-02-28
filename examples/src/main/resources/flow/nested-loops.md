# nested-loops.taq

$ java -jar taq.jar nested-loops

    Running query sort_axiom in global scope 
    sorted(1, 3, 5, 8, 12)

### Description

nested-loops.taq demonstrates a loop nested inside a loop. The aim is to sort a list 
of numbers into ascending order. As it is an insert sort being implemented, the outer 
loop feeds in the next number to be inserted and the inner loop does a shuffle to maintain 
the desired number ordering. 

You can see that the inner loop is a single term in the outer loop. It can also be 
observed that a nested loop can see all the variables declared in the enclosing flows. 
In this case, iteration variable "i" of the "insert_sort" flow is referenced by the 
outer loop

>  j = i - 1

and "j" and "next" of the outer loop are referenced by the inner loop

> ? next < sorted[j],

Note that the list being sorted is exported under an alias to reflect it's final state

> **export list\<term\>** sorted = **list** unsorted