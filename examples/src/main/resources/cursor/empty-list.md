# empty-list.taq

$ java -jar taq.jar empty-list

    Running query empty_list in global scope 
    empty_list(value=NaN, backward=-1, inc=NaN, dec=NaN, index=-1, isFact=false)

### Description

empty-list.taq demonstrates that a cursor deals gracefully when assigned to an empty 
list. The cursor declaration is normal, but the "high_cities" axiom list to which it 
is assigned is not populated before a sequence of operations is performed

> cursor city(high_cities)

Note that `NaN` is a literal value which allows a query to fail gracefully when it 
is encountered in arithmetic operations.

- Value at current cursor location: city = `NaN`
- List index after reversing direction: -city = -1
- Value post increment: city`++` = `NaN`
- Value post decrement: city`--` = `NaN`
- Current list index: city.index() = -1
- Fact status is false: fact city = false

