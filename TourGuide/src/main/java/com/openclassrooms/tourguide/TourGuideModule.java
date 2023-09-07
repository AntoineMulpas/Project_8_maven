package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.gpsUtil.GpsUtil;
import com.openclassrooms.tourguide.rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.service.RewardsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TourGuideModule {
	
	@Bean
	public GpsUtil getGpsUtil() {
		return new GpsUtil();
	}
	
	@Bean
	public RewardsService getRewardsService() {
		return new RewardsService(getGpsUtil(), getRewardCentral());
	}
	
	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}
	
}
