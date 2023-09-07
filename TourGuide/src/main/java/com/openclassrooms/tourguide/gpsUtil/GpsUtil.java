package com.openclassrooms.tourguide.gpsUtil;

import com.google.common.util.concurrent.RateLimiter;
import com.openclassrooms.tourguide.gpsUtil.location.Attraction;
import com.openclassrooms.tourguide.gpsUtil.location.Location;
import com.openclassrooms.tourguide.gpsUtil.location.VisitedLocation;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GpsUtil {
    private static final RateLimiter rateLimiter = RateLimiter.create(1000.0);

    public GpsUtil() {
    }

    public VisitedLocation getUserLocation(UUID userId) {
        rateLimiter.acquire();
        this.sleep();
        double longitude = ThreadLocalRandom.current().nextDouble(-180.0, 180.0);
        longitude = Double.parseDouble(String.format("%.6f", longitude));
        double latitude = ThreadLocalRandom.current().nextDouble(-85.05112878, 85.05112878);
        latitude = Double.parseDouble(String.format("%.6f", latitude));
        VisitedLocation visitedLocation = new VisitedLocation(userId, new Location(latitude, longitude), new Date());
        return visitedLocation;
    }

    public List <Attraction> getAttractions() {
        rateLimiter.acquire();
        this.sleepLighter();
        List<Attraction> attractions = new ArrayList();
        attractions.add(new Attraction("Disneyland", "Anaheim", "CA", 33.817595, -117.922008));
        attractions.add(new Attraction("Jackson Hole", "Jackson Hole", "WY", 43.582767, -110.821999));
        attractions.add(new Attraction("Mojave National Preserve", "Kelso", "CA", 35.141689, -115.510399));
        attractions.add(new Attraction("Joshua Tree National Park", "Joshua Tree National Park", "CA", 33.881866, -115.90065));
        attractions.add(new Attraction("Buffalo National River", "St Joe", "AR", 35.985512, -92.757652));
        attractions.add(new Attraction("Hot Springs National Park", "Hot Springs", "AR", 34.52153, -93.042267));
        attractions.add(new Attraction("Kartchner Caverns State Park", "Benson", "AZ", 31.837551, -110.347382));
        attractions.add(new Attraction("Legend Valley", "Thornville", "OH", 39.937778, -82.40667));
        attractions.add(new Attraction("Flowers Bakery of London", "Flowers Bakery of London", "KY", 37.131527, -84.07486));
        attractions.add(new Attraction("McKinley Tower", "Anchorage", "AK", 61.218887, -149.877502));
        attractions.add(new Attraction("Flatiron Building", "New York City", "NY", 40.741112, -73.989723));
        attractions.add(new Attraction("Fallingwater", "Mill Run", "PA", 39.906113, -79.468056));
        attractions.add(new Attraction("Union Station", "Washington D.C.", "CA", 38.897095, -77.006332));
        attractions.add(new Attraction("Roger Dean Stadium", "Jupiter", "FL", 26.890959, -80.116577));
        attractions.add(new Attraction("Texas Memorial Stadium", "Austin", "TX", 30.283682, -97.732536));
        attractions.add(new Attraction("Bryant-Denny Stadium", "Tuscaloosa", "AL", 33.208973, -87.550438));
        attractions.add(new Attraction("Tiger Stadium", "Baton Rouge", "LA", 30.412035, -91.183815));
        attractions.add(new Attraction("Neyland Stadium", "Knoxville", "TN", 35.955013, -83.925011));
        attractions.add(new Attraction("Kyle Field", "College Station", "TX", 30.61025, -96.339844));
        attractions.add(new Attraction("San Diego Zoo", "San Diego", "CA", 32.735317, -117.149048));
        attractions.add(new Attraction("Zoo Tampa at Lowry Park", "Tampa", "FL", 28.012804, -82.469269));
        attractions.add(new Attraction("Franklin Park Zoo", "Boston", "MA", 42.302601, -71.086731));
        attractions.add(new Attraction("El Paso Zoo", "El Paso", "TX", 31.769125, -106.44487));
        attractions.add(new Attraction("Kansas City Zoo", "Kansas City", "MO", 39.007504, -94.529625));
        attractions.add(new Attraction("Bronx Zoo", "Bronx", "NY", 40.852905, -73.872971));
        attractions.add(new Attraction("Cinderella Castle", "Orlando", "FL", 28.419411, -81.5812));
        return attractions;
    }

    private void sleep() {
        int random = ThreadLocalRandom.current().nextInt(30, 100);

        try {
            TimeUnit.MILLISECONDS.sleep((long)random);
        } catch (InterruptedException var3) {
        }

    }

    private void sleepLighter() {
        try {
            TimeUnit.MILLISECONDS.sleep(10L);
        } catch (InterruptedException var2) {
        }

    }
}
