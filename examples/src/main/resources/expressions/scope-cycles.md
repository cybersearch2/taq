# scope-cycles.taq

$ java -jar taq.jar scope-cycles

```
Running query first_time in global scope 
indexes(phase=1, one 1, two 2, three 3, four 4, random=232)
indexes(phase=2, one 1, two 2, three 3, four 7, random=232)

Running query second_time in global scope 
indexes(phase=1, one 1, two 2, three 3, four 4, random=436)
indexes(phase=2, one 1, two 2, three 3, four 7, random=436)
```

### Description

scope-cycles.taq demonstrates how variables declared in a scope behave from one query 
to the next. There are 3 declared scopes named, in order, "one", "two" and "three". 
Each of these scopes plus the global scope have an integer variable named "index" with
a chain of dependency starting with scope "one" depending on the global scope. The 
global scope also has a variable named "random" which is set to a pseudo-random number.

An "indexes" template is used to display all the index values, but with a twist. The global 
index comes last and has it's value updated from 1 to 4. The phase 1 result of query "first_time" 
is as expected, showing the declared scopes are initialized in the order of occurrence.

The phase 2 result shows all but the "Four" value are unchanged atfer the previous 
template unifcation/evaluation cycle. Global values are unaffected by template cycles 
unless updated by the template.

The result of query "scond_time" is the same as the first , except for the random term.
This shows the global index is reset to it's initial default value of 1 at completion 
of the first query. The random number change from the first to second query result 
confirms the global values were reevaluated iat the start of the second query. 