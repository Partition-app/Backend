package com.partition.domain.homeshare.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HomeLocationRequest {

    private double lat;
    private double lng;
    private int radius;
}
