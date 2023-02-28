## asia_top_ten.taq

$ java -jar taq.jar asia_top_ten

    Running query asia_top_ten in global scope 
    asia_top_ten(1, city=Tokyo, country=Japan, population=37,900,000)
    asia_top_ten(2, city=Delhi, country=India, population=26,580,000)
    asia_top_ten(3, city=Seoul, country=South Korea, population=26,100,000)
    asia_top_ten(4, city=Shanghai, country=China, population=25,400,000)
    asia_top_ten(5, city=Mumbai, country=India, population=23,920,000)
    asia_top_ten(6, city=Beijing, country=China, population=21,650,000)
    asia_top_ten(7, city=Jakarta, country=Indonesia, population=20,500,000)
    asia_top_ten(8, city=Karachi, country=Pakistan, population=20,290,000)
    asia_top_ten(9, city=Osaka, country=Japan, population=20,260,000)
    asia_top_ten(10, city=Manila, country=Philippines, population=20,040,000)

### Description

asia_top_ten.taq provides an example of filtering data according to a couple of conditions. 
In this case we are selecting the ten top Asian cities from a list of 30 highly populated 
cities from 5 continents. The list is already sorted by population size, so this makes
getting the top 10 cities simply a matter of selecting the first 10 cities.

 The expression used to filter the incoming cities tests firstly for the continent of Asia 
 and then establishes if the city is one of the first 10
 
 > rank ?? Continent == "Asia" && rank++ < 10
 
 Here we see the `??` operator which allows any boolean expression to be used to test 
 for a condition.
 
 The "rank" variable is of interest because it is both declared as a private integer 
 in the template scope and yet it's value appears as the first item in each city record 
 returned from the query. The value is inserted in an anonymous term using the **term** keyword
 
 > **term** rank