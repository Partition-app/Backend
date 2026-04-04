package com.partition.domain.calender.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDailyResponse {
    private String category;     // "CHORE" or "SCHEDULE"
    private Long id;             // 각 엔티티의 PK
    private String title;        // 제목 (내용)
    private String assigneeName; // 담당자 또는 작성자 이름
    private Boolean isCompleted; // 완료 여부 (일정은 false)

}