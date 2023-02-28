# continent-scopes2.taq

$ java -jar taq.jar continent-scopes2

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

continent-scopes2.taq illustrates address identifiers where an '@' symbol is used to 
signify the scope part of the identifier. The only change from the original program 
is to use addresses in the query declarations

> **query\<axiom\>** euro_megacities (mega_city\@ \: \@Europe.megacities) 

The "mega_city@" identifier is an artifact address that references an axiom source 
in the global scope. In this case, the name of the scope can be omitted. The full address 
is "mega_city@global".

The "@Europe.megacities" identifier is a template address signified by having the "@' 
symbol at the start of the identifier. A template address is always a 2-part name, 
even for the global scope.

Whether to use addresses in place of plain identifiers is almost without exception 
a matter of preference. Despite the low occurrence of addresses in the examples, it 
is recommended to use addresses to reference artifacts that are declared other than
the immidiate vicinity.


