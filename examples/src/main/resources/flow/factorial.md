# factorial.taq

$ java -jar taq.jar factorial

Running query factorial4 in global scope 
factorial4(n=4, factorial=24)

Running query factorial5 in global scope 
factorial5(n=5, factorial=120)

### Description

factorial.taq demonstrates a compact loop being used to calculate the factorial of  
numbers 4 and 5. 

>{ ?? (i++ < n) factorial *= i }

Here 'i' is a loop variable which starts at zero and iterates until it matches the
natural number 'n' being evaluated. Note tnat if n = 1 then there is no loopiing at 
all. 

The loop is just a sideways detour and the flow of execution resumes along it's original 
course after the loop exits..