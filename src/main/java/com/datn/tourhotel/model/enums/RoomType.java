package com.datn.tourhotel.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomType {

    SINGLE(1),
    DOUBLE(2),
	SUITE(3);

    private final int capacity;

}
