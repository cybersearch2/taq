## assign-marks.taq

$ java -jar taq.jar assign-marks

    Running query marks in global scope 
    score(student=George, english=b+, maths=b-, history=a-)
    score(student=Sarah, english=c+, maths=a, history=b+)
    score(student=Amy, english=b, maths=a-, history=e+)

### Description

assign-marks.taq shows a list being created with 18 items and has an index range of 1 to 
18 instead of 0 to 17. The list is also initialized in reverse as it is intuitive that 
alpha grades appear in descending order.

The list is declared specifying the custom range

> list\<string\> mark[1,18] = **reverse** ( "a+","a","a-" ... "f+","f","f-" }

Another detail of interest is how the values of untyped variables "english",
"maths" and "history" change when the "score" template is evaluated. 

> english = mark[english], maths = mark[maths], history = mark[history]

These variables are initially set to integer scores by unification, but each is assigned 
a text alpha grade to resolve the query. 