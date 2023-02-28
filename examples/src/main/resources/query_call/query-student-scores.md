# query-student-scores.taq

$ java -jar taq.jar query-student-scores
```
Running query marks in global scope 
student_marks(report=George English:b+ Math:b- History:a-)
student_marks(report=Sarah English:c+ Math:a History:b+)
student_marks(report=Amy English:b Math:a- History:e+)
```
### Description

query-student-scores.taq demonstrates making a query to a flow using a function call operation. 
The "report" flow belongs to the "school" scope and is designed to take 3 integer 
numbers, convert each one into a subject + alpha mark axiom and return an axiom list result in 
a term named "subjects". The term which is exported is an axiom list declaration

> **list\<axiom\>** subjects

The following terms populate the iist and are made private as, given their inclusion in the 
subjects list, they are redundant. Notice that only the first list concatenation 
term needs to be explicitly marked as private to make all three private.

The operation to run the query to the template looks like a function call, The fact 
it performs a query is hidden, but there is no disguising the fact that a flow 
only returns a term list. When the list contains only a single term, as is the case 
here, it makes sense to reference the term immediately after the function call returns

> **cursor\<axiom\>** subjects = 
>
>> school.report(english, maths, history)-\>subjects

This invokes a query to the "report" flow with parameters "english", "maths" and "history" 
and the "subjects" result is extracted from the returned term list. You will notice that the 
terms of the "report" template scope align with the parameters of the call.
