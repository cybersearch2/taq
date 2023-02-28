# dynamic-grouping.taq

$ java -jar taq.jar dynamic-grouping

    Running query mega_cities_by_continent in global scope 
    africa(Continent=Africa, Megacity=Cairo, Country=Egypt)
    africa(Continent=Africa, Megacity=Lagos, Country=Nigeria)
    africa(Continent=Africa, Megacity=Kinshasa, Country=Democratic Republic of Congo)
    asia(Continent=Asia, Megacity=Tokyo, Country=Japan)
    ...
    asia(Continent=Asia, Megacity=Hyderabad, Country=India)
    europe(Continent=Europe, Megacity=Moscow, Country=Russia)
    europe(Continent=Europe, Megacity=London, Country=United Kingdom)
    europe(Continent=Europe, Megacity=Istanbul, Country=Turkey)
    europe(Continent=Europe, Megacity=Rhine-Ruhr, Country=Germany)
    europe(Continent=Europe, Megacity=Paris, Country=France)
    north_america(Continent=North America, Megacity=Mexico City, Country=Mexico)
    north_america(Continent=North America, Megacity=New York City, Country=United States)
    north_america(Continent=North America, Megacity=Los Angeles, Country=United States)
    south_america(Continent=South America, Megacity=Sao Paulo, Country=Brazil)
    south_america(Continent=South America, Megacity=Buenos Aires, Country=Argentina)
    south_america(Continent=South America, Megacity=Rio de Janeiro, Country=Brazil)

### Description

dynamic-grouping.taq demonstrates grouping using a **map** operation to route each item 
to a specific group list. This allows the grouping to be completed in a single pass 
through the incoming collection. The query result shows there are 5 different export 
lists, one for each continent - africa, asia, europe, north_america, south_america.
The **map** is qualified with the keyword **list** to indicate each mapping target 
is a list. Note the list names are identifiers, not text,

```
mega_city = list map Continent  {
    ? "Asia":          asia
    ? "Africa":        africa
    ? "Europe":        europe
    ? "South America": south_america
    ? "North America": north_america
}
```
