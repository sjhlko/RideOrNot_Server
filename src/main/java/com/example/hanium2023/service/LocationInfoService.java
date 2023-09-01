package com.example.hanium2023.service;

import com.example.hanium2023.domain.dto.publicapi.location.LocationInfoApiResult;
import com.example.hanium2023.domain.dto.publicapi.location.LocationInfoPushAlarm;
import com.example.hanium2023.domain.dto.user.MovingSpeedInfo;
import com.example.hanium2023.domain.dto.user.UserDto;
import com.example.hanium2023.domain.entity.Station;
import com.example.hanium2023.enums.TrainStatusCodeEnum;
import com.example.hanium2023.enums.MovingMessageEnum;
import com.example.hanium2023.repository.StationRepository;
import com.example.hanium2023.repository.UserRepository;
import com.example.hanium2023.util.JsonUtil;
import com.example.hanium2023.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationInfoService {
    private final StationRepository stationRepository;
    private final PublicApiService publicApiService;
    private final JsonUtil jsonUtil;
    private final UserRepository userRepository;
    private final RedisUtil redisUtil;


    public List<LocationInfoPushAlarm> getLocationInfoForPushAlarm(String stationName, String exitName) {
        List<Station> stationList = stationRepository.findAllByStatnName(stationName);
        List<LocationInfoApiResult> locationInfoApiResultList = new ArrayList<>();
        for (Station s : stationList) {
            locationInfoApiResultList.addAll(getLocationInfoFromPublicApi(s.getLine().getLineName()));
        }

        UserDto userDto = new UserDto(userRepository.findById(38L).get());

        return locationInfoApiResultList
                .stream()
                .filter(locationInfoApiResult -> locationInfoApiResult.getStationName().equals(stationName))
                .filter(locationInfoApiResult -> locationInfoApiResult.getTrainStatusCode() == TrainStatusCodeEnum.DEPART_BEFORE_STATION.getCode())
                .map(LocationInfoPushAlarm::new)
                .map(this::calculateArrivalTime)
                .map(apiResult -> calculateMovingTime(apiResult, stationName, exitName, userDto))
                .collect(Collectors.toList());
    }

    private LocationInfoPushAlarm calculateArrivalTime(LocationInfoPushAlarm locationInfoPushAlarm) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime targetTime = LocalDateTime.parse(locationInfoPushAlarm.getCreatedAt(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Station station = stationRepository.findByStatnNameAndLine_LineId(locationInfoPushAlarm.getStationName(), Integer.valueOf(locationInfoPushAlarm.getLineId()));
        Integer adjacentStationTime = locationInfoPushAlarm.getDirection().equals("상행") ? station.getBeforeStationTime1() : station.getNextStationTime1();
        int timeGap = (int) Duration.between(currentTime, targetTime.plusSeconds(adjacentStationTime)).getSeconds();
        locationInfoPushAlarm.setArrivalTime(timeGap);
        return locationInfoPushAlarm;
    }

    public List<LocationInfoApiResult> getLocationInfoFromPublicApi(String lineName) {
        JSONObject apiResultJsonObject = publicApiService.getApiResult(publicApiService.getLocationApiUrl(lineName));
        Optional<JSONArray> jsonArray = Optional.ofNullable((JSONArray) apiResultJsonObject.get("realtimePositionList"));
        List<LocationInfoApiResult> locationInfoApiResult = new ArrayList<>();

        if (jsonArray.isPresent()) {
            locationInfoApiResult = jsonUtil.convertJsonArrayToDtoList(jsonArray.get(), LocationInfoApiResult.class);
        }
        return locationInfoApiResult;
    }

    private LocationInfoPushAlarm calculateMovingTime(LocationInfoPushAlarm locationInfoPushAlarm, String stationName, String exitName, UserDto userDto) {
        Integer stationId = redisUtil.getStationIdByStationNameAndLineId(stationName, Integer.valueOf(locationInfoPushAlarm.getLineId()));
        double distance = redisUtil.getDistanceByStationIdAndExitName(stationId, exitName);
        double minMovingSpeed = distance / (double) locationInfoPushAlarm.getArrivalTime();

        MovingSpeedInfo movingSpeedInfo = getMovingSpeedInfo(userDto, minMovingSpeed);

        long movingTime = (long) (distance / movingSpeedInfo.getMovingSpeed());

        if (movingSpeedInfo.getMovingSpeed() == -1.0)
            locationInfoPushAlarm.setMessage(movingSpeedInfo.getMovingMessageEnum().getMessage());
        else
            locationInfoPushAlarm.setMessage(movingTime + "초 동안 " + movingSpeedInfo.getMovingMessageEnum().getMessage());

        locationInfoPushAlarm.setMovingTime(movingTime > 0 ? movingTime : 0);
        locationInfoPushAlarm.setMovingSpeedStep(movingSpeedInfo.getMovingMessageEnum().getMovingSpeedStep());
        locationInfoPushAlarm.setMovingSpeed(movingSpeedInfo.getMovingSpeed());

        return locationInfoPushAlarm;
    }

    private MovingSpeedInfo getMovingSpeedInfo(UserDto userDto, double minMovingSpeed) {
        MovingMessageEnum[] movingMessageEnums = MovingMessageEnum.getMovingMessageEnums();
        double[] speedBoundary = getSpeedBoundary(userDto);

        for (int i = 0; i < speedBoundary.length; i++) {
            if (minMovingSpeed <= speedBoundary[i]) {
                return new MovingSpeedInfo(movingMessageEnums[i], speedBoundary[i]);
            }
        }

        return new MovingSpeedInfo(MovingMessageEnum.CANNOT_BOARD, -1.0);
    }

    private double[] getSpeedBoundary(UserDto userDto) {
        return new double[]{
                userDto.getWalkingSpeed(),
                userDto.getWalkingSpeed() * 1.2,
                userDto.getWalkingSpeed() * 1.5,
                userDto.getRunningSpeed() * 0.5,
                userDto.getRunningSpeed(),
                userDto.getRunningSpeed() * 1.2
        };
    }
}
