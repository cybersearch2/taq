# perfect-match2.taq

$ java -jar taq.jar perfect-match2

```
apply_age_rating(Name=John, Age=23, Starsign=gemini, Rating=1.0, Timestamp=2022-09-23T08:00:51.889212Z)
apply_age_rating(Name=Sue, Age=19, Starsign=cancer, Rating=NaN, Timestamp=2022-09-23T08:00:52.050077Z)
apply_age_rating(Name=Sam, Age=34, Starsign=scorpio, Rating=0.3, Timestamp=2022-09-23T08:00:52.172256Z)
apply_age_rating(Name=Jenny, Age=28, Starsign=gemini, Rating=0.6, Timestamp=2022-09-23T08:00:52.306504Z)
apply_age_rating(Name=Andrew, Age=26, Starsign=virgo, Rating=0.6, Timestamp=2022-09-23T08:00:52.440881Z)
apply_age_rating(Name=Alice, Age=20, Starsign=pisces, Rating=1.0, Timestamp=2022-09-23T08:00:52.575231Z)
apply_age_rating(Name=Ingrid, Age=23, Starsign=cancer, Rating=1.0, Timestamp=2022-09-23T08:00:52.698360Z)
apply_age_rating(Name=Jack, Age=32, Starsign=pisces, Rating=0.3, Timestamp=2022-09-23T08:00:52.822988Z)
apply_age_rating(Name=Sonia, Age=33, Starsign=gemini, Rating=0.3, Timestamp=2022-09-23T08:00:52.957377Z)
apply_age_rating(Name=Alex, Age=22, Starsign=aquarius, Rating=1.0, Timestamp=2022-09-23T08:00:53.091543Z)
apply_age_rating(Name=Jill, Age=33, Starsign=cancer, Rating=0.3, Timestamp=2022-09-23T08:00:53.236504Z)
apply_age_rating(Name=Fiona, Age=29, Starsign=gemini, Rating=0.6, Timestamp=2022-09-23T08:00:53.359992Z)
apply_age_rating(Name=Melissa, Age=30, Starsign=virgo, Rating=0.3, Timestamp=2022-09-23T08:00:53.482911Z)
apply_age_rating(Name=Tom, Age=22, Starsign=cancer, Rating=1.0, Timestamp=2022-09-23T08:00:53.606223Z)
apply_age_rating(Name=Bill, Age=19, Starsign=virgo, Rating=NaN, Timestamp=2022-09-23T08:00:53.740342Z)
```

### Description

perfect-match2.taq demonstrates a database resource data collector with an entity 
class used to define the database records. The "star_people" query creates a dating profile 
for each person in a database that is 20 years old and over. 

The resource declaration does not specify a identifies a provider, so one is selected 
automatically to use with the H2 database. Only the database file needs 
to be specified

> **resource** star_people 
>
>> (database="db/star-people.db", type="H2") {
 
When the file is specified with a relative path, as is the case here, then it is located 
relative to the "workspace" sub directory from where the taq application is launched. 
The entity class, in double quotes, is referenced using a `->` right arrow after the 
role qualifier

> template apply_age_rating 
>
>> -\> "entities_axioms.StarPerson"

As with resource providers, entity classes are external to TAQ and are loaded from 
the file system on demand.

There are 2 resource operations in the resource declaration body

- `star_people.drop_tables()` restores the database to an empty state if it exists
- `star_people.log_to_console()` sends to the console records being written to the database 

Note that the above console output shows the rating value of 19 year old Sue and Bill 
as `NaN` which is the Java double "not an number" token. SqLite does not natively support 
the Java double type, so TAQ takes measures to work around this limitation. 
If you browse the database with a suitable tool, you will see the persisted rating values 
are encoded.

