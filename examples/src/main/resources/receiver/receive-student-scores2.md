# receive-student-scores2.taq

java -jar taq.jar receive-student-scores2
```
Running query marks in global scope 
student_marks(report=George english:b+ maths:b- history:a-)
student_marks(report=Sarah english:c+ maths:a history:b+)
student_marks(report=Amy english:b maths:a- history:e+)
```
### Description

receive-student-scores2.taq demonstrates how to return an axiom list from a function 
call to a template which only has fixed return type of term list. Function "school.report()" 
converts 3 numeric scores to an axiom list containing subject + alpha mark records. 
A "subjects" term declares an axiom list which is exported by the "report" flow

> **list\<axiom\>** subjects

The receiver contains a curosr which binds to a variable named "subjects"

> **flow** school.report(english, maths, history) {
>
>> **cursor\<axiom\>** subjects,
