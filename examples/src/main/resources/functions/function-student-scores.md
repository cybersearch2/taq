# function-student-scores.taq

$ java -jar taq.jar function-student-scores

```
Running query marks in global scope 
student_marks(report=George english:b+ maths:b- history:a-)
student_marks(report=Sarah english:c+ maths:a history:b+)
student_marks(report=Amy english:b maths:a- history:e+)
```
### Description

function-student-scores.taq demonstrates a function which returns an axiom list declared as an 
"alpha_grades" template archetype. Each axiom in this list has a "subject" term and 
a "mark" term. Both values are derived from an integer term with a subject name and 
in this example there are 3 subjects passed as function parameters - english, maths 
and history.

> **template\<axiom\>** alpha_grades (**string** subject, **string** mark)

Note how the provider is declared is in the "school' scope and the provider Java class is specified 
as a "provider" scope parameter.

> **scope** school(provider = "school.SchoolFunctionProvider")

The function is declared inside this scope as returning an "alpha_grades" list.

> **axiom\<alpha_grades\>** **function** convert_grades(**integer** marks...)

Also note how the list returned from the function call is assigned to a cursor
with the same name as the function

> **cursor\<axiom\>** convert_grades
