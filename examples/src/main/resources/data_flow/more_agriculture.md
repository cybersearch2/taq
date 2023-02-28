## more_agriculture.taq

$ java -jar taq.jar more_agriculture

    Running query more_agriculture in global scope 
    surface_area_increase(country=Albania, surface_area=986)
    surface_area_increase(country=Algeria, surface_area=25722)
    surface_area_increase(country=American Samoa, surface_area=10)
    ... (65 records in total)
    surface_area_increase(country=Vietnam, surface_area=41381)
    surface_area_increase(country=Zambia, surface_area=25212)
    surface_area_increase(country=Zimbabwe, surface_area=34777)

### Description

more_agriculture.taq demonstrates numerical analysis of data. The 'more_agriculture' 
query produces a list of countries which have increased the area
under agriculture by more than 1% over the twenty years between 1990 and 2010.

The query is performed in 2 stages. The first stage filters countries which meet the 
surface area increase criteria. This stage is chained to the second stage which converts 
the percentage increase to surface area.

Note the final surface area is automatically converted from a double value to an integer to 
make the presentation of the results more readable than if fractions are included.  