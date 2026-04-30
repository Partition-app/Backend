package com.partition.domain.reservation.service;

import com.partition.domain.reservation.dto.request.CreateReservationRequest;
import com.partition.domain.reservation.dto.response.CreateReservationResponse;
import com.partition.domain.reservation.exception.ReservationErrorCode;
import com.partition.domain.reservation.repository.ReservationItemRepository;
import com.partition.domain.reservation.repository.ReservationRepository;
import com.partition.domain.user.exception.UserErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Reservation;
import com.partition.entity.ReservationItem;
import com.partition.entity.User;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateReservationResponse createReservation(Long userId, CreateReservationRequest request) {
        if (request.getItemId() == null) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2001);
        }
        if (request.getStartTime() == null) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2002);
        }
        if (request.getEndTime() == null) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2003);
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2004);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        ReservationItem item = reservationItemRepository.findById(request.getItemId())
                .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_1003));

        if (reservationRepository.existsOverlap(item, request.getStartTime(), request.getEndTime())) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2005);
        }

        Reservation reservation = Reservation.builder()
                .item(item)
                .user(user)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        reservationRepository.save(reservation);

        return CreateReservationResponse.from(reservation);
    }
}
