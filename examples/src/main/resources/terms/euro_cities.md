# euro_cities.md

$ java -jar taq.jar euro_cities

```
Running query euro_megacities in global scope 
megacities(Megacity=Moscow, Country=Russia)
megacities(Megacity=London, Country=United Kingdom)
megacities(Megacity=Istanbul, Country=Turkey)
megacities(Megacity=Rhine-Ruhr, Country=Germany)
megacities(Megacity=Paris, Country=France)
```

### Description

euro_cities.taq demonstrates using a period '**.**' to prevent a term being passed 
to the solution. It is placed at the start of the line.

> **.** Continent ? "Europe"