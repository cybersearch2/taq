# perfect-match.taq

$ java -jar taq.jar perfect-match

    Running query star_people in global scope 
    apply_age_rating(name=John, age=23, starsign=gemini, generation=twilight)
    apply_age_rating(name=Sam, age=34, starsign=scorpio, generation=mooner)
    apply_age_rating(name=Jenny, age=28, starsign=gemini, generation=starlight)
    apply_age_rating(name=Andrew, age=26, starsign=virgo, generation=starlight)
    apply_age_rating(name=Alice, age=20, starsign=pices, generation=twilight)
    apply_age_rating(name=Ingrid, age=23, starsign=cancer, generation=twilight)
    apply_age_rating(name=Jack, age=32, starsign=pisces, generation=mooner)
    apply_age_rating(name=Sonia, age=33, starsign=gemini, generation=mooner)
    apply_age_rating(name=Alex, age=22, starsign=aquarius, generation=twilight)
    apply_age_rating(name=Jill, age=33, starsign=cancer, generation=mooner)
    apply_age_rating(name=Fiona, age=29, starsign=gemini, generation=starlight)
    apply_age_rating(name=Melissa, age=30, starsign=virgo, generation=mooner)
    apply_age_rating(name=Tom, age=22, starsign=cancer, generation=twilight)

### Description
    
perfect-match.taq shows selection default strategy of simply skipping over items that 
fail to match any of the available choices. This only works in a template as it relies 
on the fact a template solution is discarded if any terms are blank. 

The "star_people" query creates a dating profile for each person in a database that 
is 20 years old and over. People 19 and under need to be excluded and this is 
achieved using a map which defines age ranges. The lower bound excludes ages 19 and 
lower

> ? >= 20:   "twilight" 

If you compare the "person" list to the query result, you will observe Sue and Bill 
are not rated as both are 10 years old. The "generation" term is set blank  by the **map**
so these people are skipped.

As an experiment you can change the "apply_age_rating" template to a flow. Sue and 
Bill then are included in the query result, but with blank generation values.
