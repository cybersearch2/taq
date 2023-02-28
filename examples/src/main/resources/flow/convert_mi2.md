# convert_mi2.md

$ java -jar taq.jar convert_mi2

    Running query convert_areas in global scope 
    convert_area(country=Antigua and Barbuda, area=440)
    convert_area(country=Australia, area=7741212)
    ...
    convert_area(country=New Zealand, area=267710)
    ...
    convert_area(country=Zambia, area=752610)

### Description
    
convert_mi2.md demonstrates a branch applied to correct an inconsistency in
the incoming data. The "convert_areas" query takes a list of British Commonwealth countries and their 
surface areas in a mix of metric and imperial units and creates a new list where they are all 
metric.

There are 2 countries which are the odd ones out - Australia and New Zealand (2,988,885 
and 103,363 square miles respectively). The surface area conversion is handled in a 
compact branch

>?? (unit == imperial) surface_area *= 2.59

The branch is just a sideways detour and the flow of execution resumes along it's original 
course after the branch is taken.