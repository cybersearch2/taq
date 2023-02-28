# types2.taq

java -jar taq.jar types2

```
Running query types in global scope 
types(
 Boolean=true,
 String=penguins,
 Integer=12345,
 Double=123400.0,
 Decimal=1234.56,
 Currency=12345.67,
 Timestamp=Mon Oct 24 06:55:15 AEDT 2022,
 Type=String
)
```
       
### Description

types2.taq demonstrates a function declaration requesting a alternative return type 
from the default. Function system.timestamp() calls system library function timestamp()
and normally it returns a Date object. The function declaration here puts the return type 
as string and this is supported.

> **scope** system { **string function** timestamp() }

Note that the timestamp displayed will vary according to locale in addition to date and time.
A second system "simple_name" function is used to display the Java class name of the
Timestamp value.
