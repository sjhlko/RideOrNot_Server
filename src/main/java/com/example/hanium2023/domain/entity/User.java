package com.example.hanium2023.domain.entity;

import com.example.hanium2023.util.converter.StringCryptoConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Convert(converter = StringCryptoConverter.class)
    private String nickname;
    private double walkingSpeed;
    private double runningSpeed;
    private double InitialWalkingSpeed;
    private double InitialRunningSpeed;
    private int ageRange;
    private int gender;
    private String password;
    public void updateWalkingSpeed(double walkingSpeed){
        this.walkingSpeed = walkingSpeed;
    }

    public void updateRunningSpeed(double runningSpeed){
        this.runningSpeed = runningSpeed;
    }
}
