package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.gpsUtil.GpsUtil;
import com.openclassrooms.tourguide.gpsUtil.location.Location;
import com.openclassrooms.tourguide.gpsUtil.location.VisitedLocation;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.models.ClosestAttractionsDTO;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.tripPricer.Provider;
import com.openclassrooms.tourguide.tripPricer.TripPricer;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TourGuideService {
	private       Logger         logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil        gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer     tripPricer = new TripPricer();
	public final  Tracker        tracker;
	boolean testMode = true;

	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		Locale.setDefault(Locale.US);

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	/**
	 * This method is used to get the reward concerning a specific user.
	 * @param user
	 * @return List<UserReward>
	 */

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}


	/**
	 * This method is used to get the current user location.
	 * If user has already a visited location, then the method returns it.
	 * Otherwise, it tracks the current user position and returns it.
	 * @param user
	 * @return VisitedLocation
	 */
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user);
		return visitedLocation;
	}

	/**
	 * This method is user for testing purpose. It returns a specific user
	 * from username.
	 * Note: the user is randomly generated.
	 * @param userName
	 * @return User
	 */

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	/**
	 * This method is used for testing purpose, it returns a List of all users.
	 * Note: the List of all users is generated randomly.
	 * @return List<User>
	 */

	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}


	/**
	 * This method is user to add a new user.
	 * Note: the user is added to a Map which is used to testing purpose.
	 * @param user
	 */

	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	/**
	 * This method is used to set the list of provider a user is associated with.
	 * According to several parameters (such as numbers of children, adults, trip duration and already cumulated reward points)
	 * the method is setting a list of Provider to a user.
	 * @param user
	 * @return List<Provider>
	 */

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	/**
	 * This method is used to track the user location.
	 * It also calls the 'calculateRewards' method from 'rewardsService' to calculte the reward associated
	 * with the current user.
	 * @param user
	 * @return VisitedLocation
	 */

	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}


	/**
	 * Method which uses ExecutorService to ameliorate performance to track user location of several users.
	 * @param userList
	 * @return Map<UUID, VisitedLocation>
	 */

	public Map<UUID, VisitedLocation> trackAllUsersLocation(List<User> userList) {
		ExecutorService service = Executors.newFixedThreadPool(30);
		Map<UUID, VisitedLocation> userVisitedLocationMap = new HashMap<>();
		try {
			for (User user : userList) {
				service.execute(() -> {
					VisitedLocation visitedLocation = trackUserLocation(user);
					userVisitedLocationMap.put(user.getUserId(), visitedLocation);
				});
			}
			service.shutdown();
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			System.out.println("All threads have completed");
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return userVisitedLocationMap;
	}

	/**
	 * This method is used to get the attraction close to the user location
	 * (represented by the object VisitedLocation).
	 * @param visitedLocation
	 * @return List<Attraction>
	 */

	public List<ClosestAttractionsDTO> getNearByAttractions(User user, VisitedLocation visitedLocation) {
		return rewardsService.getTopFiveNearestAttraction(user, visitedLocation.location);
	}


	/**
	 * This method is used to shut down the tracker.
	 */

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
					new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

}
