## grouping.taq

$java -jar taq.jar grouping

```
Running query mega_cities_by_continent in global scope 
group_by_continent(
 continent=Asia,
 city=Tokyo,
 country=Japan,
 rank=1,
 population=37,900,000
)
...
group_by_continent(
 continent=Africa,
 city=Cairo,
 country=Egypt,
 rank=14,
 population=18,810,000
)
...
group_by_continent(
 continent=Europe,
 city=Moscow,
 country=Russia,
 rank=17,
 population=16,900,000
) 
...
group_by_continent(
 continent=South America,
 city=Sao Paulo,
 country=Brazil,
 rank=8,
 population=21,390,000
)
...
group_by_continent(
 continent=North America,
 city=Mexico City,
 country=Mexico,
 rank=6,
 population=22,200,000
)
```

### Description

grouping.taq shows a simple way to sort data into categories, otherwise known as "grouping". 
The categories here are defined in the "continents" axiom list and the order of the 
continents in this list is reflected in the query solution.

A more advanced approach to grouping is given in dynamic-grouping.taq of the "select" examples.