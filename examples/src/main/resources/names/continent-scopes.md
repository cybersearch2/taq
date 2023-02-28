# continent-scopes.taq

$ java -jar taq.jar continent-scopes

    Running query asia_megacities in global scope 
    megacities(Megacity=Tokyo, Country=Japan, Continent=Asia)
    megacities(Megacity=Delhi, Country=India, Continent=Asia)
    megacities(Megacity=Seoul, Country=South Korea, Continent=Asia)
    megacities(Megacity=Shanghai, Country=China, Continent=Asia)
    megacities(Megacity=Mumbai, Country=India, Continent=Asia)
    megacities(Megacity=Beijing, Country=China, Continent=Asia)
    megacities(Megacity=Jakarta, Country=Indonesia, Continent=Asia)
    megacities(Megacity=Karachi, Country=Pakistan, Continent=Asia)
    megacities(Megacity=Osaka, Country=Japan, Continent=Asia)
    megacities(Megacity=Manila, Country=Philippines, Continent=Asia)
    megacities(Megacity=Dhaka, Country=Bangladesh, Continent=Asia)
    megacities(Megacity=Kolkata, Country=India, Continent=Asia)
    megacities(Megacity=Bangkok, Country=Thailand, Continent=Asia)
    megacities(Megacity=Tehran, Country=Iran, Continent=Asia)
    megacities(Megacity=Guangzhou, Country=China, Continent=Asia)
    megacities(Megacity=Shenzhen, Country=China, Continent=Asia)
    megacities(Megacity=Lahore, Country=Pakistan, Continent=Asia)
    megacities(Megacity=Tianjin, Country=China, Continent=Asia)
    megacities(Megacity=Bengaluru, Country=India, Continent=Asia)
    megacities(Megacity=Chennai, Country=India, Continent=Asia)
    megacities(Megacity=Hyderabad, Country=India, Continent=Asia)

    Running query euro_megacities in global scope 
    megacities(Megacity=Moscow, Country=Russia, Continent=Europe)
    megacities(Megacity=London, Country=United Kingdom, Continent=Europe)
    megacities(Megacity=Istanbul, Country=Turkey, Continent=Europe)
    megacities(Megacity=Rhine-Ruhr, Country=Germany, Continent=Europe)
    megacities(Megacity=Paris, Country=France, Continent=Europe)

### Description

continent-scopes.taq shows how an identifier can refer to a specific scope by creating 
a 2-part name where the first part is the scope name. Here there are 2 scopes representing 
continents "Asia" and "Europe" and 2 scope-specific queries - asia_megacities and euro_megacities.
The difference between the two queries is how they refer to the "megacities" template - 
"Asia.megacities" and "Europe.megacities" respectively. 

The template executes in the scope by which it is referenced and the selection logic 
is formed by the expression

> continent ? **scope**.name
    
This means select the current Megacity axiom if it's continent value matches the name of 
the current scope. You can see that "scope.name" instead of being a normal 2-part name, 
is a notation to get the name of the current scope.

The included"mega_city.taq" file is located in the examples resources root location 
which is the parent folder of the one in which the continent-scopes.taq. file is located. 
It lists 30 cities across 5 continents.


