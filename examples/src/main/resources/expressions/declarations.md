## declarations.taq

$ java -jar taq.jar declarations

    Running query query_sample in global scope 
    sample(d=1234.56)

### Description

declarations.taq reveals important details about variable declarations made in what 
is called the "template scope". This is optionally placed before the template body. enclosed 
in `{}` braces and contains declarations that are private to the template. One important 
reason for privacy may be to prevent input data being relayed to the template solution.
What is demonstrated in this example

- template scope variables must be typed and cannot be assigned a value

- template scope variables are private, which is why the query solution only contains 
term "d"

- it is good practice to use template scope variables for collecting input data

- numeric types all have a default value of zero, hence check `a ? a == 0` is correct
  
