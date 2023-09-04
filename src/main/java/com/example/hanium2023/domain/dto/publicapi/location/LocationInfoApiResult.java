package com.example.hanium2023.domain.dto.publicapi.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationInfoApiResult {
//    (0 : 상행/내선, 1 : 하행/외선)
    @JsonProperty("updnLine")
    private Integer direction;

    @JsonProperty("subwayNm")
    private String lineName;
    @JsonProperty("subwayId")
    private Integer lineId;

    @JsonProperty("recptnDt")
    private String createdAt;

    @JsonProperty("statnNm")
    private String stationName;

    @JsonProperty("statnId")
    private Integer stationId;

    @JsonProperty("statnTnm")
    private String lastStationName;

    @JsonProperty("statnTid")
    private Integer lastStationId;

    @JsonProperty("trainSttus")
    private Integer trainStatusCode;

    @JsonProperty("directAt")
    private Integer isExpress;

    @JsonProperty("lstcarAt")
    private Integer isLastTrain;

    @JsonProperty("trainNo")
    private Integer trainNumber;

    @Override
    public String toString() {
        return "LocationInfoApiResult{" +
                "direction=" + direction +
                ", lineName='" + lineName + '\'' +
                ", lineId=" + lineId +
                ", createdAt='" + createdAt + '\'' +
                ", stationName='" + stationName + '\'' +
                ", stationId=" + stationId +
                ", lastStationName='" + lastStationName + '\'' +
                ", lastStationId=" + lastStationId +
                ", trainStatus=" + trainStatusCode +
                ", isExpress=" + isExpress +
                ", isLastTrain=" + isLastTrain +
                '}';
    }
}
