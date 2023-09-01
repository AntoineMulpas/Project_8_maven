package com.openclassrooms.tourguide.service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.openclassrooms.tourguide.models.ClosestAttractionsDTO;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private final int defaultProximityBuffer = 10;
	private       int proximityBuffer        = defaultProximityBuffer;
	private final int     attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	private List<Attraction> attractions;
	
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;

		getAttractions();
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	/**
	 * This method is used to get List of all attractions.
	 */
	private void getAttractions() {
		attractions = gpsUtil.getAttractions();
	}

	public User calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();

		for (int i = 0; i < userLocations.size(); i++) {
			for(Attraction attraction : attractions) {
				if(user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
					VisitedLocation visitedLocation = userLocations.get(i);
					if(nearAttraction(visitedLocation, attraction)) {
						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}
				}
			}
		}

		return user;
	}

	public List<User> calculateRewardsForAllUsers(List<User> userList) {
		List<User> listToReturn = new ArrayList <>();

		ExecutorService service = Executors.newFixedThreadPool(50);
		try {

			userList.forEach(user -> {
				service.execute(() -> {
					if (user != null) {
						listToReturn.add(calculateRewards(user));
					}
				});
			});

			service.shutdown();
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return listToReturn;
	}


	//TODO: Resume to implement method to sort attractions by proximity

	public List<ClosestAttractionsDTO> getTopFiveNearestAttraction(User user, Location location) {
		Map <Double, Attraction> map = new TreeMap<>(Comparator.naturalOrder());
		List<Attraction> topFive = new ArrayList<>();

		attractions.forEach(attraction -> {
			double distance = getDistance(attraction, location);
			map.put(distance, attraction);
		});

		int count = 0;
		for (Map.Entry<Double, Attraction> entry : map.entrySet()) {
			if (count >= 5) {
				break;
			}
			topFive.add(entry.getValue());
			count++;
		}


		return closestAttractionsDTOS(user, topFive,location);
	}


	private List<ClosestAttractionsDTO> closestAttractionsDTOS(User user,List<Attraction> attractions, Location location) {
		List<ClosestAttractionsDTO> closestAttractionsDTOS = new ArrayList<>();
		for (Attraction attraction : attractions) {
			closestAttractionsDTOS.add( new ClosestAttractionsDTO(
					attraction.attractionName,
					attraction.longitude,
					attraction.latitude,
					location.longitude,
					location.latitude,
					getDistance(attraction, location),
					user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum()
			));
		}
		return closestAttractionsDTOS;
	}





	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return (getDistance(attraction, location) < attractionProximityRange);
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return (getDistance(attraction, visitedLocation.location) < proximityBuffer);
	}
	
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
		return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
	}

}
