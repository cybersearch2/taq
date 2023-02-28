## american_megacities.taq

$ java -jar taq.jar american_megacities

```
Running query american_megacities in global scope 
american_megacities(
 Megacity=Mexico City,
 Country=Mexico,
 Continent=North America
)
american_megacities(
 Megacity=Sao Paulo,
 Country=Brazil,
 Continent=South America
)
american_megacities(
 Megacity=New York City,
 Country=United States,
 Continent=North America
)
american_megacities(
 Megacity=Los Angeles,
 Country=United States,
 Continent=North America
)
american_megacities(
 Megacity=Buenos Aires,
 Country=Argentina,
 Continent=South America
)
american_megacities(
 Megacity=Rio de Janeiro,
 Country=Brazil,
 Continent=South America
)
```

### Description

american_megacities.taq demonstrates a logic selection set. which contains one or more 
comma-delimited items to match on and enclosed in `{}` braces. The "american_megacities" 
template selects cities in either of the North and South America continents

> Continent { "North America", "South America" }