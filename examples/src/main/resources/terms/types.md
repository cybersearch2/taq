## types.taq

$ java -jar taq.jar types

```
types(
 Boolean=true,
 String=penguins,
 Integer=12345,
 Double=123400.0,
 Decimal=1234.56,
 Currency=12345.67
)
```

### Description

types.taq shows the result of unifying a template containing 6 untyped variables with 
an axiom containing literal terms of 6 different types. The "types" query returns a 
single axiom with terms that reflect the original literal values. 

The most interesting case is the currency type which starts out as Euro literal value 
"12.345,67 €" and is persisted as decimal value 12345.67. Also note the decimal literal 
is expressed as

> **decimal** 1234.56

The **decimal** keyword distinguishes the numeric value from it's **double** equivalent.