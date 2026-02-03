package com.gaekdam.gaekdambe.dummy.generate.operation_service.room;

import com.gaekdam.gaekdambe.operation_service.room.command.domain.entity.Room;
import com.gaekdam.gaekdambe.operation_service.room.command.domain.entity.RoomType;
import com.gaekdam.gaekdambe.operation_service.room.command.infrastructure.repository.RoomRepository;
import com.gaekdam.gaekdambe.operation_service.room.command.infrastructure.repository.RoomTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Component
public class DummyRoomDataTest {

    @Autowired
    private RoomRepository roomRepository;

    @Transactional
    public void generate() {

        if (roomRepository.count() > 0) {
            return;
        }

        long roomTypeCode = 1;

        for (int hotel = 1; hotel <= 10; hotel++) {

            // 호텔당 RoomType 5개
            for (int type = 1; type <= 5; type++) {

                // RoomType 하나당 객실 10개
                for (int i = 1; i <= 10; i++) {

                    int floor = 1 + i; // 2~11층
                    int roomNumber = floor * 100 + type;

                    Room room = Room.createRoom(
                            roomNumber,
                            floor,
                            roomTypeCode
                    );

                    roomRepository.save(room);
                }

                roomTypeCode++; // 다음 RoomType
            }
        }
    }
}
